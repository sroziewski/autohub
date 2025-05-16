package com.autohub.user_service.infrastructure.messaging;

import com.autohub.user_service.infrastructure.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumer for audit log messages from the audit log queue.
 * Processes audit log messages asynchronously.
 */
@Component
@Slf4j
public class AuditLogConsumer {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AuditLogConsumer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Processes audit log messages from the audit log queue.
     *
     * @param auditLogMessage The audit log message to process
     */
    @RabbitListener(queues = RabbitMQConfig.AUDIT_LOG_QUEUE)
    public void processAuditLogMessage(AuditLogMessage auditLogMessage) {
        log.debug("Received audit log message from queue: {}", auditLogMessage);
        
        try {
            // Insert audit log into database
            jdbcTemplate.update(
                "INSERT INTO autohub.audit_logs (user_id, action, resource_type, resource_id, ip_address, user_agent, timestamp, details, success) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                auditLogMessage.getUserId(),
                auditLogMessage.getAction(),
                auditLogMessage.getResourceType(),
                auditLogMessage.getResourceId(),
                auditLogMessage.getIpAddress(),
                auditLogMessage.getUserAgent(),
                auditLogMessage.getTimestamp(),
                auditLogMessage.getDetails(),
                auditLogMessage.isSuccess()
            );
            
            log.debug("Successfully processed audit log message: {}", auditLogMessage);
        } catch (Exception e) {
            log.error("Error processing audit log message: {}", auditLogMessage, e);
            // Re-throw to trigger retry mechanism
            throw new RuntimeException("Error processing audit log message", e);
        }
    }
}
