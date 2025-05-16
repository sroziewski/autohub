package com.autohub.user_service.infrastructure.security.oauth2;

import com.autohub.user_service.domain.entity.User;
import com.autohub.user_service.application.service.UserService;
import com.autohub.user_service.infrastructure.configuration.JwtProperties;
import com.autohub.user_service.infrastructure.security.JwtUtil;
import com.autohub.user_service.presentation.dto.user.RegisterUserRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Handler for successful OAuth2 authentication.
 * Generates a JWT token and redirects to the frontend with the token as a query parameter.
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                       Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            // Generate JWT token
            String token = jwtUtil.generateToken(oauth2User);

            // Redirect to frontend with token
            String redirectUrl = UriComponentsBuilder.fromUriString(jwtProperties.getRedirectUri())
                    .queryParam("token", token)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
