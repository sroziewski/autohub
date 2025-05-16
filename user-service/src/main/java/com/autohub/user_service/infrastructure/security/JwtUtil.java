package com.autohub.user_service.infrastructure.security;

import com.autohub.user_service.infrastructure.configuration.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private final Key key;

    // Cache the JWT parser to avoid recreating it for each token validation
    private final JwtParser jwtParser;

    // Optional cache for validation results (username -> token -> validation result)
    private final Map<String, Map<String, Boolean>> validationCache = new ConcurrentHashMap<>();

    // Maximum size of the validation cache per user
    private static final int MAX_CACHE_SIZE_PER_USER = 5;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
        // Initialize the JWT parser once and reuse it for all token validations
        this.jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        // Use the cached parser instead of creating a new one each time
        return jwtParser.parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Generate a token with a session ID
     *
     * @param userDetails User details
     * @param sessionId Session ID to include in the token
     * @return JWT token
     */
    public String generateTokenWithSessionId(UserDetails userDetails, UUID sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sessionId", sessionId.toString());
        return createToken(claims, userDetails.getUsername());
    }

    public String generateToken(OAuth2User oauth2User) {
        Map<String, Object> claims = new HashMap<>();
        String email = oauth2User.getAttribute("email");
        if (email == null) {
            throw new IllegalArgumentException("OAuth2 user must have an email attribute");
        }
        return createToken(claims, email);
    }

    /**
     * Generate a token with a session ID for OAuth2 user
     *
     * @param oauth2User OAuth2 user
     * @param sessionId Session ID to include in the token
     * @return JWT token
     */
    public String generateTokenWithSessionId(OAuth2User oauth2User, UUID sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sessionId", sessionId.toString());
        String email = oauth2User.getAttribute("email");
        if (email == null) {
            throw new IllegalArgumentException("OAuth2 user must have an email attribute");
        }
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        long validity = jwtProperties.getTokenValidityInSeconds() * 1000;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + validity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = userDetails.getUsername();

        // Check cache first
        Map<String, Boolean> userTokenCache = validationCache.get(username);
        if (userTokenCache != null && userTokenCache.containsKey(token)) {
            return userTokenCache.get(token);
        }

        try {
            // Extract claims in one operation
            Claims claims = extractAllClaims(token);

            // Check if token is expired
            boolean isExpired = claims.getExpiration().before(new Date());

            // Check if username matches
            boolean isValid = claims.getSubject().equals(username) && !isExpired;

            // Cache the result
            cacheValidationResult(username, token, isValid);

            return isValid;
        } catch (Exception e) {
            return false;
        }
    }

    private void cacheValidationResult(String username, String token, boolean isValid) {
        // Only cache valid tokens
        if (isValid) {
            validationCache.computeIfAbsent(username, k -> new ConcurrentHashMap<>())
                    .put(token, true);

            // Limit cache size per user
            Map<String, Boolean> userTokenCache = validationCache.get(username);
            if (userTokenCache.size() > MAX_CACHE_SIZE_PER_USER) {
                // Remove a random entry if cache is too large
                String keyToRemove = userTokenCache.keySet().iterator().next();
                userTokenCache.remove(keyToRemove);
            }
        }
    }

    /**
     * Clears all expired tokens from the validation cache.
     * This method can be called periodically to prevent memory leaks.
     */
    public void clearExpiredTokensFromCache() {
        for (String username : validationCache.keySet()) {
            Map<String, Boolean> userTokenCache = validationCache.get(username);
            if (userTokenCache != null) {
                userTokenCache.keySet().removeIf(token -> {
                    try {
                        return isTokenExpired(token);
                    } catch (Exception e) {
                        // If we can't parse the token, remove it from cache
                        return true;
                    }
                });

                // Remove user from cache if they have no tokens
                if (userTokenCache.isEmpty()) {
                    validationCache.remove(username);
                }
            }
        }
    }
}
