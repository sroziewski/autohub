package com.autohub.user_service.infrastructure.security;

import com.autohub.user_service.infrastructure.configuration.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final JwtProperties jwtProperties;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, JwtProperties jwtProperties) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader(jwtProperties.getHeaderName());

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith(jwtProperties.getTokenPrefix())) {
            jwt = authorizationHeader.substring(jwtProperties.getTokenPrefix().length());
            try {
                // Extract username first to check if we need to load user details
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("jwt.token.error");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Only load user details if we have a username and no authentication
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Validate token - this will use the cached validation result if available
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
