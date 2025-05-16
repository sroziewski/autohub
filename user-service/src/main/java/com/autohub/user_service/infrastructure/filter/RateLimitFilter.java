package com.autohub.user_service.infrastructure.filter;

import com.autohub.user_service.infrastructure.service.RateLimitService;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to apply rate limiting to requests.
 * This filter intercepts all requests and applies rate limiting based on the client IP address and request path.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip rate limiting for static resources and non-API paths
        String path = request.getRequestURI();
        if (isStaticResource(path) || !isApiPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Try to consume a token from the rate limiter
        ConsumptionProbe probe = rateLimitService.tryConsume(request);

        if (probe.isConsumed()) {
            // Request is allowed, add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.addHeader("X-Rate-Limit-Reset", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
            response.getWriter().write("Rate limit exceeded. Please try again later.");
        }
    }

    /**
     * Determines if the request is for a static resource.
     *
     * @param path the request path
     * @return true if the path is for a static resource
     */
    private boolean isStaticResource(String path) {
        return path.startsWith("/static/") || 
               path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/images/") || 
               path.startsWith("/favicon.ico");
    }

    /**
     * Determines if the request is for an API path that should be rate limited.
     *
     * @param path the request path
     * @return true if the path is an API path
     */
    private boolean isApiPath(String path) {
        return path.startsWith("/auth/") || 
               path.startsWith("/users/") || 
               path.startsWith("/api/");
    }
}
