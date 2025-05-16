package com.autohub.user_service.infrastructure.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom health indicator that provides detailed information about the service status.
 * This includes database connectivity, user count, and system information.
 */
@Component
public class ServiceHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ServiceHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        // Add system information
        details.put("jvm.version", System.getProperty("java.version"));
        details.put("os.name", System.getProperty("os.name"));
        details.put("os.version", System.getProperty("os.version"));
        details.put("os.arch", System.getProperty("os.arch"));
        details.put("available.processors", Runtime.getRuntime().availableProcessors());
        details.put("free.memory", Runtime.getRuntime().freeMemory());
        details.put("total.memory", Runtime.getRuntime().totalMemory());
        details.put("max.memory", Runtime.getRuntime().maxMemory());
        
        try {
            // Check database connectivity and get user count
            Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM autohub.users", Integer.class);
            details.put("user.count", userCount);
            
            return Health.up()
                    .withDetails(details)
                    .build();
        } catch (Exception e) {
            details.put("error", e.getMessage());
            return Health.down()
                    .withDetails(details)
                    .build();
        }
    }
}
