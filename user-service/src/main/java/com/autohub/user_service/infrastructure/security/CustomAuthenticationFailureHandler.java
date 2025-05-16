package com.autohub.user_service.infrastructure.security;

import com.autohub.user_service.application.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication failure handler that tracks failed login attempts
 * and locks accounts after a certain number of failures.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, 
                                       AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("email");
        
        if (email != null && !email.isEmpty()) {
            // Check if the account is already locked
            if (userService.isAccountLocked(email)) {
                throw new LockedException("error.auth.account_locked");
            }
            
            // Record the failed login attempt
            boolean locked = userService.recordFailedLoginAttempt(email);
            
            if (locked) {
                throw new LockedException("error.auth.account_locked");
            }
        }
        
        // Delegate to the default failure handler
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed: " + exception.getMessage());
    }
}
