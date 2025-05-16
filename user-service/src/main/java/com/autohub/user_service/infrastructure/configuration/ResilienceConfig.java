package com.autohub.user_service.infrastructure.configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration class for Resilience4j circuit breaker patterns.
 * This class configures circuit breakers for external service calls.
 */
@Configuration
public class ResilienceConfig {

    private static final String EMAIL_SERVICE_CIRCUIT_BREAKER = "emailServiceCircuitBreaker";

    /**
     * Creates a CircuitBreakerRegistry with custom configuration for the EmailService.
     * 
     * @return CircuitBreakerRegistry with configured circuit breakers
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig emailServiceConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)                // When 50% of calls fail
                .waitDurationInOpenState(Duration.ofSeconds(30))  // Wait 30 seconds in OPEN state before transitioning to HALF_OPEN
                .permittedNumberOfCallsInHalfOpenState(5)  // Allow 5 calls in HALF_OPEN state
                .slidingWindowSize(10)                   // Consider last 10 calls for failure rate calculation
                .recordExceptions(Exception.class)       // Record all exceptions as failures
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(CircuitBreakerConfig.ofDefaults());
        registry.addConfiguration(EMAIL_SERVICE_CIRCUIT_BREAKER, emailServiceConfig);
        
        // Create the circuit breaker instance
        registry.circuitBreaker(EMAIL_SERVICE_CIRCUIT_BREAKER);
        
        return registry;
    }

    /**
     * Creates a TimeLimiterConfig for the EmailService.
     * This limits the duration of calls to prevent long-running operations.
     * 
     * @return TimeLimiterConfig for EmailService
     */
    @Bean
    public TimeLimiterConfig emailServiceTimeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))  // Timeout after 5 seconds
                .build();
    }
}
