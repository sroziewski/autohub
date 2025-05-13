package com.autohub.user_service.presentation.controller;

import com.autohub.user_service.presentation.dto.AuthenticationRequest;
import com.autohub.user_service.presentation.dto.AuthenticationResponse;
import com.autohub.user_service.infrastructure.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

import static com.autohub.user_service.domain.util.MessageUtils.getMessage;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthenticationController(AuthenticationManager authenticationManager,
                                    UserDetailsService userDetailsService,
                                    JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest, Locale locale) throws Exception {
        System.out.println(getMessage("validation.role.current.required", locale));
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(),
                            authenticationRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new Exception(getMessage("error.auth.invalid_credentials", locale), e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
}
