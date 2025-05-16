package com.autohub.user_service.infrastructure.scheduler;

import com.autohub.user_service.infrastructure.configuration.JwtProperties;
import com.autohub.user_service.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to periodically clean up the JWT validation cache.
 * This prevents memory leaks by removing expired tokens from the cache.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtCacheCleanupTask {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    /**
     * Cleans up expired tokens from the JWT validation cache at a configurable interval.
     * This prevents the cache from growing indefinitely with expired tokens.
     */
    @Scheduled(fixedRateString = "${application.jwt.cache-cleanup-interval:3600000}")
    public void cleanupExpiredTokens() {
        log.debug("Starting scheduled cleanup of expired JWT tokens from cache");
        jwtUtil.clearExpiredTokensFromCache();
        log.debug("Completed cleanup of expired JWT tokens from cache");
    }
}
