package com.autohub.user_service.infrastructure.security.oauth2;

import com.autohub.user_service.application.service.UserService;
import com.autohub.user_service.domain.entity.User;
import com.autohub.user_service.domain.entity.UserStatus;
import com.autohub.user_service.presentation.dto.user.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service to process OAuth2 user information and create or retrieve users.
 */
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        // Process the user
        processOAuth2User(userRequest, oauth2User);
        
        return oauth2User;
    }
    
    private void processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oauth2User.getName();
        String email = oauth2User.getAttribute("email");
        
        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
        
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            // Create new user
            RegisterUserRequest registerRequest = createRegisterRequest(oauth2User, provider);
            userService.registerUser(registerRequest);
            // TODO: Update user with OAuth2 provider info
        } else {
            // User exists, update if needed
            // TODO: Update user with OAuth2 provider info if needed
        }
    }
    
    private RegisterUserRequest createRegisterRequest(OAuth2User oauth2User, String provider) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");
        
        // If first/last name not provided directly, try to extract from full name
        if ((firstName == null || lastName == null) && name != null) {
            String[] nameParts = name.split(" ");
            if (nameParts.length > 0) {
                if (firstName == null) {
                    firstName = nameParts[0];
                }
                if (lastName == null && nameParts.length > 1) {
                    lastName = nameParts[nameParts.length - 1];
                }
            }
        }
        
        // Ensure we have values for required fields
        if (firstName == null) firstName = "User";
        if (lastName == null) lastName = provider.substring(0, 1).toUpperCase() + provider.substring(1);
        
        // Generate a secure random password
        String password = UUID.randomUUID().toString();
        
        return RegisterUserRequest.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password(passwordEncoder.encode(password))
                .build();
    }
}
