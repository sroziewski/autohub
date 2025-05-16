package com.autohub.user_service.infrastructure.configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for rate limiting.
 * This class defines the rate limits for different endpoints.
 */
@Configuration
public class RateLimitConfig {

    // Cache to store rate limiters for each IP address
    private final Map<String, Bucket> authenticationCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registrationCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> verificationCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> generalCache = new ConcurrentHashMap<>();

    /**
     * Creates a rate limiter for authentication endpoints.
     * Allows 5 requests per minute.
     *
     * @param ipAddress the IP address of the client
     * @return a rate limiter bucket
     */
    public Bucket resolveBucketForAuthentication(String ipAddress) {
        return authenticationCache.computeIfAbsent(ipAddress, ip -> createAuthenticationBucket());
    }

    /**
     * Creates a rate limiter for registration endpoints.
     * Allows 3 requests per minute.
     *
     * @param ipAddress the IP address of the client
     * @return a rate limiter bucket
     */
    public Bucket resolveBucketForRegistration(String ipAddress) {
        return registrationCache.computeIfAbsent(ipAddress, ip -> createRegistrationBucket());
    }

    /**
     * Creates a rate limiter for verification endpoints.
     * Allows 10 requests per minute.
     *
     * @param ipAddress the IP address of the client
     * @return a rate limiter bucket
     */
    public Bucket resolveBucketForVerification(String ipAddress) {
        return verificationCache.computeIfAbsent(ipAddress, ip -> createVerificationBucket());
    }

    /**
     * Creates a rate limiter for general API endpoints.
     * Allows 30 requests per minute.
     *
     * @param ipAddress the IP address of the client
     * @return a rate limiter bucket
     */
    public Bucket resolveBucketForGeneral(String ipAddress) {
        return generalCache.computeIfAbsent(ipAddress, ip -> createGeneralBucket());
    }

    private Bucket createAuthenticationBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket4j.builder().addLimit(limit).build();
    }

    private Bucket createRegistrationBucket() {
        Bandwidth limit = Bandwidth.classic(3, Refill.greedy(3, Duration.ofMinutes(1)));
        return Bucket4j.builder().addLimit(limit).build();
    }

    private Bucket createVerificationBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket4j.builder().addLimit(limit).build();
    }

    private Bucket createGeneralBucket() {
        Bandwidth limit = Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(1)));
        return Bucket4j.builder().addLimit(limit).build();
    }

    /**
     * Cleans up expired buckets to prevent memory leaks.
     * This method should be called periodically.
     */
    public void cleanupExpiredBuckets() {
        // Remove entries that haven't been accessed in a while
        // This is a simple implementation; in production, you might want to use a more sophisticated approach
        authenticationCache.clear();
        registrationCache.clear();
        verificationCache.clear();
        generalCache.clear();
    }
}
