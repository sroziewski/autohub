package com.autohub.user_service.infrastructure.security;

import com.autohub.user_service.infrastructure.configuration.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private JwtProperties jwtProperties;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("testSecretKeyWithAtLeast32Characters1234567890");
        jwtProperties.setTokenValidityInSeconds(3600);
        
        jwtUtil = new JwtUtil(jwtProperties);
        
        userDetails = User.withUsername("test@example.com")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
    }

    @Test
    void generateAndValidateToken() {
        // Generate token
        String token = jwtUtil.generateToken(userDetails);
        
        // Validate token
        boolean isValid = jwtUtil.validateToken(token, userDetails);
        
        // Assert token is valid
        assertTrue(isValid, "Token should be valid");
    }
    
    @Test
    void validateTokenMultipleTimes() {
        // Generate token
        String token = jwtUtil.generateToken(userDetails);
        
        // Validate token multiple times to test caching
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            assertTrue(jwtUtil.validateToken(token, userDetails), "Token should be valid");
        }
        long endTime = System.nanoTime();
        
        System.out.println("[DEBUG_LOG] Time taken to validate token 1000 times: " + 
                (endTime - startTime) / 1_000_000 + " ms");
    }
    
    @Test
    void clearExpiredTokensFromCache() {
        // Generate token with short validity
        jwtProperties.setTokenValidityInSeconds(1); // 1 second validity
        String token = jwtUtil.generateToken(userDetails);
        
        // Validate token (adds to cache)
        assertTrue(jwtUtil.validateToken(token, userDetails), "Token should be valid initially");
        
        // Wait for token to expire
        try {
            Thread.sleep(1500); // Wait 1.5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Clear expired tokens
        jwtUtil.clearExpiredTokensFromCache();
        
        // Token should now be invalid
        assertFalse(jwtUtil.validateToken(token, userDetails), "Token should be invalid after expiration");
    }
}
