package com.autohub.user_service.infrastructure.messaging;

import com.autohub.user_service.infrastructure.config.RabbitMQConfig;
import com.autohub.user_service.infrastructure.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumer for email messages from the email queue.
 * Processes email messages asynchronously.
 */
@Component
@Slf4j
public class EmailConsumer {

    private final EmailService emailService;

    @Autowired
    public EmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Processes email messages from the email queue.
     *
     * @param emailMessage The email message to process
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void processEmailMessage(EmailMessage emailMessage) {
        log.debug("Received email message from queue: {}", emailMessage);

        try {
            // Send email using template
            emailService.sendGenericEmail(
                    emailMessage.getTo(),
                    emailMessage.getSubject(),
                    emailMessage.getTemplateName(),
                    emailMessage.getTemplateVariables() != null ? 
                        emailMessage.getTemplateVariables() : 
                        Map.of("plainText", emailMessage.getPlainText())
            );
            log.debug("Successfully processed email message: {}", emailMessage);
        } catch (Exception e) {
            log.error("Error processing email message: {}", emailMessage, e);

            // If retry count is less than 3, increment and re-queue
            if (emailMessage.getRetryCount() < 3) {
                emailMessage.setRetryCount(emailMessage.getRetryCount() + 1);
                log.debug("Re-queuing email message for retry {}: {}", emailMessage.getRetryCount(), emailMessage);
                throw new RuntimeException("Error processing email message, re-queuing for retry", e);
            } else {
                log.error("Max retry count reached for email message, discarding: {}", emailMessage);
            }
        }
    }
}
