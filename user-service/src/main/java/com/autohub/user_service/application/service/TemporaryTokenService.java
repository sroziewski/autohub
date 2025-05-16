package com.autohub.user_service.application.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * Service for handling temporary tokens used during two-factor authentication.
 */
@Service
public class TemporaryTokenService {

    private final Key key;
    private final long tempTokenValidity;

    public TemporaryTokenService(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.temp-token-validity:300}") long tempTokenValidity) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.tempTokenValidity = tempTokenValidity * 1000; // Convert to milliseconds
    }

    /**
     * Generates a temporary token for a user during two-factor authentication.
     *
     * @param userId The user ID
     * @return A temporary token
     */
    public String generateTemporaryToken(UUID userId) {
        long now = System.currentTimeMillis();
        Date validity = new Date(now + tempTokenValidity);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date(now))
                .setExpiration(validity)
                .claim("type", "2fa_temp")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extracts the user ID from a temporary token.
     *
     * @param token The temporary token
     * @return The user ID
     */
    public UUID getUserIdFromToken(String token) {
        String subject = getClaimFromToken(token, Claims::getSubject);
        return UUID.fromString(subject);
    }

    /**
     * Validates a temporary token.
     *
     * @param token The temporary token
     * @return true if the token is valid
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            
            // Check if token is expired
            if (claims.getExpiration().before(new Date())) {
                return false;
            }
            
            // Check if token is a temporary 2FA token
            String tokenType = claims.get("type", String.class);
            return "2fa_temp".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
