package com.autohub.user_service.infrastructure.security;

import com.autohub.user_service.application.service.SessionService;
import com.autohub.user_service.domain.entity.Session;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to track user sessions
 * This filter creates a session when a user logs in, updates the last active time
 * when a user makes a request, and adds the session ID to the request attributes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionTrackingFilter extends OncePerRequestFilter {

    private final SessionService sessionService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        try {
            if (token != null) {
                String username = jwtUtil.extractUsername(token);
                UUID userId = UUID.fromString(username);

                // We don't have UserDetails here, so we can only do basic validation
                // Full validation happens in JwtAuthenticationFilter

            // Check if session ID is in the token
            UUID sessionId = extractSessionId(token);

            if (sessionId != null) {
                // Session exists, update last active time
                sessionService.updateLastActive(sessionId)
                        .ifPresent(session -> {
                            // Add session ID to request attributes for use in controllers
                            request.setAttribute("sessionId", session.getId());
                            log.debug("Updated last active time for session {}", session.getId());
                        });
            } else {
                // No session ID in token, create a new session
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    Session session = sessionService.createSession(userId, request);
                    request.setAttribute("sessionId", session.getId());
                    log.debug("Created new session {} for user {}", session.getId(), userId);
                }
            }
            }
        } catch (Exception e) {
            log.debug("Error processing JWT token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from request
     * 
     * @param request HTTP request
     * @return JWT token or null if not found
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Extract session ID from JWT token
     * 
     * @param token JWT token
     * @return Session ID or null if not found
     */
    private UUID extractSessionId(String token) {
        try {
            Claims claims = jwtUtil.extractAllClaims(token);
            Object sessionIdObj = claims.get("sessionId");
            if (sessionIdObj != null) {
                String sessionIdStr = sessionIdObj.toString();
                if (!sessionIdStr.isEmpty()) {
                    try {
                        return UUID.fromString(sessionIdStr);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid session ID in token: {}", sessionIdStr);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error extracting session ID from token: {}", e.getMessage());
        }
        return null;
    }
}
