package com.autohub.user_service.infrastructure.task;

import com.autohub.user_service.infrastructure.service.RateLimitService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to clean up expired rate limit buckets.
 */
@Component
public class RateLimitCleanupTask {

    private final RateLimitService rateLimitService;

    public RateLimitCleanupTask(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    /**
     * Cleans up expired rate limit buckets every hour.
     */
    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 ms)
    public void cleanupExpiredBuckets() {
        rateLimitService.cleanupExpiredBuckets();
    }
}
