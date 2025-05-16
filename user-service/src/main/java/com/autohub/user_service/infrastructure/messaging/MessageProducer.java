package com.autohub.user_service.infrastructure.messaging;

import com.autohub.user_service.infrastructure.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for sending messages to RabbitMQ queues.
 * This service is used for asynchronous processing of non-critical operations.
 */
@Service
@Slf4j
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Sends an email message to the email queue.
     *
     * @param emailMessage The email message to send
     */
    public void sendEmailMessage(EmailMessage emailMessage) {
        log.debug("Sending email message to queue: {}", emailMessage);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_SERVICE_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                emailMessage
        );
    }

    /**
     * Sends an audit log message to the audit log queue.
     *
     * @param auditLogMessage The audit log message to send
     */
    public void sendAuditLogMessage(AuditLogMessage auditLogMessage) {
        log.debug("Sending audit log message to queue: {}", auditLogMessage);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_SERVICE_EXCHANGE,
                RabbitMQConfig.AUDIT_LOG_ROUTING_KEY,
                auditLogMessage
        );
    }

    /**
     * Sends a notification message to the notification queue.
     *
     * @param notificationMessage The notification message to send
     */
    public void sendNotificationMessage(NotificationMessage notificationMessage) {
        log.debug("Sending notification message to queue: {}", notificationMessage);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_SERVICE_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                notificationMessage
        );
    }
}
