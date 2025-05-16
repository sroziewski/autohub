package com.autohub.user_service.presentation.controller;

import com.autohub.user_service.application.service.UserService;
import com.autohub.user_service.infrastructure.security.JwtUtil;
import com.autohub.user_service.presentation.dto.auth.AuthenticationRequest;
import com.autohub.user_service.presentation.dto.auth.AuthenticationResponse;
import com.autohub.user_service.presentation.dto.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthenticationController(AuthenticationManager authenticationManager,
                                    UserDetailsService userDetailsService,
                                    JwtUtil jwtUtil,
                                    UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    /**
     * Authenticate a user and generate a JWT token
     *
     * @param authenticationRequest The authentication request containing email and password
     * @return A response containing the JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> createAuthenticationToken(
            @Valid @RequestBody AuthenticationRequest authenticationRequest) {

        String email = authenticationRequest.getEmail();
        log.info("Authentication attempt for user: {}", email);

        // Check if account is locked before attempting authentication
        if (userService.isAccountLocked(email)) {
            log.warn("Authentication attempt for locked account: {}", email);
            throw new LockedException("Account is locked");
        }

        try {
            // Attempt authentication
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            authenticationRequest.getPassword()
                    )
            );

            // Authentication successful, reset failed login attempts
            log.info("Authentication successful for user: {}", email);
            userService.findByEmail(email).ifPresent(user -> userService.recordLogin(user.getId()));

        } catch (BadCredentialsException e) {
            // Record failed login attempt
            log.warn("Failed authentication attempt for user: {}", email);
            boolean locked = userService.recordFailedLoginAttempt(email);
            if (locked) {
                log.warn("Account locked after failed authentication attempts: {}", email);
                throw new LockedException("Account is locked after multiple failed attempts");
            }
            throw new BadCredentialsException("Invalid credentials");
        }

        // Generate JWT token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        final String jwt = jwtUtil.generateToken(userDetails);

        // Return successful response with token
        return ResponseEntity.ok(ApiResponse.success(new AuthenticationResponse(jwt)));
    }
}
