package com.autohub.user_service.infrastructure.service;

import com.autohub.user_service.infrastructure.configuration.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * Service for applying rate limiting to requests.
 */
@Service
public class RateLimitService {

    private final RateLimitConfig rateLimitConfig;

    public RateLimitService(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    /**
     * Tries to consume a token from the appropriate bucket based on the request path.
     *
     * @param request the HTTP request
     * @return a consumption probe indicating whether the request is allowed and remaining tokens
     */
    public ConsumptionProbe tryConsume(HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String path = request.getRequestURI();

        Bucket bucket;
        if (path.startsWith("/auth/login")) {
            bucket = rateLimitConfig.resolveBucketForAuthentication(ipAddress);
        } else if (path.startsWith("/users/register")) {
            bucket = rateLimitConfig.resolveBucketForRegistration(ipAddress);
        } else if (path.startsWith("/users/verify")) {
            bucket = rateLimitConfig.resolveBucketForVerification(ipAddress);
        } else {
            bucket = rateLimitConfig.resolveBucketForGeneral(ipAddress);
        }

        return bucket.tryConsumeAndReturnRemaining(1);
    }

    /**
     * Extracts the client IP address from the request.
     * Handles cases where the request might be coming through a proxy.
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, the first one is the client
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Schedules cleanup of expired buckets.
     * This method should be called periodically, e.g., by a scheduled task.
     */
    public void cleanupExpiredBuckets() {
        rateLimitConfig.cleanupExpiredBuckets();
    }
}
