package com.autohub.user_service.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to log HTTP requests and responses with structured information.
 * Populates MDC with request details for structured logging.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Wrap request and response to allow reading the body multiple times
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        
        // Generate unique request ID
        String requestId = UUID.randomUUID().toString();
        
        // Start time for performance measurement
        long startTime = System.currentTimeMillis();
        
        try {
            // Populate MDC with request information
            MDC.put("requestId", requestId);
            MDC.put("clientIp", getClientIp(request));
            MDC.put("endpoint", request.getRequestURI());
            MDC.put("httpMethod", request.getMethod());
            
            // Add user ID if authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                MDC.put("userId", authentication.getName());
            }
            
            // Log request
            log.info("Request received: {} {}", request.getMethod(), request.getRequestURI());
            
            // Continue with the filter chain
            filterChain.doFilter(requestWrapper, responseWrapper);
            
            // Log response
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("executionTime", String.valueOf(duration));
            MDC.put("statusCode", String.valueOf(responseWrapper.getStatus()));
            
            log.info("Request completed: {} {} - Status: {} - Duration: {}ms", 
                    request.getMethod(), 
                    request.getRequestURI(),
                    responseWrapper.getStatus(),
                    duration);
            
        } finally {
            // Copy content to the original response
            responseWrapper.copyBodyToResponse();
            
            // Clear MDC to prevent memory leaks
            MDC.clear();
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
