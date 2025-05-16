package com.autohub.user_service.presentation.controller;

import com.autohub.user_service.application.service.UserService;
import com.autohub.user_service.infrastructure.security.JwtUtil;
import com.autohub.user_service.presentation.dto.auth.AuthenticationRequest;
import com.autohub.user_service.presentation.dto.auth.AuthenticationResponse;
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

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        String email = authenticationRequest.getEmail();

        // Check if account is locked before attempting authentication
        if (userService.isAccountLocked(email)) {
            throw new Exception("error.auth.account_locked");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            authenticationRequest.getPassword()
                    )
            );

            // Authentication successful, reset failed login attempts
            // Find the user by email and record login
            userService.findByEmail(email).ifPresent(user -> userService.recordLogin(user.getId()));

        } catch (BadCredentialsException e) {
            // Record failed login attempt
            boolean locked = userService.recordFailedLoginAttempt(email);
            if (locked) {
                throw new Exception("error.auth.account_locked");
            }
            throw new Exception("error.auth.invalid_credentials");
        } catch (LockedException e) {
            throw new Exception("error.auth.account_locked");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
}
