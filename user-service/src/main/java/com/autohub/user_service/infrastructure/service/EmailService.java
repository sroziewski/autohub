package com.autohub.user_service.infrastructure.service;

import com.autohub.user_service.domain.entity.User;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String EMAIL_SERVICE_CIRCUIT_BREAKER = "emailServiceCircuitBreaker";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker circuitBreaker;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.url}")
    private String appUrl;

    /**
     * Initialize the circuit breaker after properties are set
     */
    @PostConstruct
    public void initCircuitBreaker() {
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(EMAIL_SERVICE_CIRCUIT_BREAKER);
        log.info("Initialized circuit breaker for email service: {}", circuitBreaker.getName());

        // Register event listeners for circuit breaker state changes
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.info("Circuit breaker '{}' state changed from {} to {}",
                        circuitBreaker.getName(), event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()))
                .onError(event -> log.error("Circuit breaker '{}' recorded an error: {}",
                        circuitBreaker.getName(), event.getThrowable().getMessage()));
    }

    /**
     * Send verification email to newly registered user
     *
     * @param user The user to send verification email to
     */
    @Async
    public void sendVerificationEmail(User user) {
        String verificationUrl = appUrl + "/users/verify?token=" + user.getVerificationToken();

        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("verificationUrl", verificationUrl);

        sendEmail(
                user.getEmail(),
                "Verify Your Email Address",
                "email/verification",
                variables
        );
    }

    /**
     * Send password reset email
     *
     * @param user User requesting password reset
     */
    @Async
    public void sendPasswordResetEmail(User user) {
        String resetUrl = appUrl + "/reset-password?token=" + user.getResetPasswordToken();

        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("resetUrl", resetUrl);
        variables.put("expiryHours", 24); // match your service's expiry time

        sendEmail(
                user.getEmail(),
                "Password Reset Request",
                "email/password-reset",
                variables
        );
    }

    /**
     * Send welcome email after user verifies account
     *
     * @param user Verified user
     */
    @Async
    public void sendWelcomeEmail(User user) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("loginUrl", appUrl + "/login");

        sendEmail(
                user.getEmail(),
                "Welcome to AutoHub!",
                "email/welcome",
                variables
        );
    }

    /**
     * Send account status change notification
     *
     * @param user User whose account status changed
     * @param newStatus The new status of the user
     */
    @Async
    public void sendStatusChangeEmail(User user, String newStatus) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", user);
        variables.put("newStatus", newStatus);
        variables.put("helpEmail", "support@autohub.com");

        sendEmail(
                user.getEmail(),
                "Your Account Status Has Changed",
                "email/status-change",
                variables
        );
    }

    /**
     * Generic method to send an email using Thymeleaf templates
     * This method uses a circuit breaker to handle failures gracefully
     *
     * @param to Email recipient
     * @param subject Email subject
     * @param templateName Name of Thymeleaf template (without .html extension)
     * @param variables Variables to pass to template
     */
    private void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        // Create email parameters object outside try block for access in catch block
        EmailParameters params = null;

        try {
            // Prepare the context with variables
            Context context = new Context();
            variables.forEach(context::setVariable);

            // Process the template
            String htmlContent = templateEngine.process(templateName, context);

            // Create email parameters object to pass to the circuit breaker
            params = new EmailParameters(to, subject, htmlContent);
            final EmailParameters finalParams = params; // Final copy for lambda

            // Execute the email sending with circuit breaker protection
            circuitBreaker.executeSupplier(() -> {
                try {
                    return sendEmailWithCircuitBreaker(finalParams);
                } catch (MessagingException e) {
                    log.error("Circuit breaker caught exception while sending email to {}: {}", 
                            to, e.getMessage());
                    throw new RuntimeException("Failed to send email", e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to send email to {} with subject '{}': {}", to, subject, e.getMessage());
            // Fallback when circuit breaker is open or execution fails
            if (params != null) {
                emailFallback(e, params);
            }
        }
    }

    /**
     * Actual email sending logic, protected by circuit breaker
     * 
     * @param params Email parameters
     * @return true if email was sent successfully
     * @throws MessagingException if there was an error sending the email
     */
    private Boolean sendEmailWithCircuitBreaker(EmailParameters params) throws MessagingException {
        // Create message
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
        );

        helper.setTo(params.to);
        helper.setFrom(fromEmail);
        helper.setSubject(params.subject);
        helper.setText(params.htmlContent, true);

        // Send email
        mailSender.send(message);
        log.info("Email sent to {} with subject '{}'", params.to, params.subject);

        return true;
    }

    /**
     * Fallback method for when the circuit is open
     * This is called automatically by the circuit breaker when the circuit is open
     * 
     * @param e The exception that caused the fallback
     * @param params Email parameters
     * @return false to indicate the email was not sent
     */
    private Boolean emailFallback(Exception e, EmailParameters params) {
        log.warn("Circuit breaker is open! Email to {} with subject '{}' was not sent. Error: {}", 
                params.to, params.subject, e.getMessage());
        // Here you could implement alternative notification methods or queue the email for later
        return false;
    }

    /**
     * Public method to send a generic email using Thymeleaf templates
     * This method is used by the asynchronous email consumer
     *
     * @param to Email recipient
     * @param subject Email subject
     * @param templateName Name of Thymeleaf template (without .html extension)
     * @param variables Variables to pass to template
     */
    @Async
    public void sendGenericEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        sendEmail(to, subject, templateName, variables);
    }

    /**
     * Simple class to hold email parameters
     */
    private static class EmailParameters {
        private final String to;
        private final String subject;
        private final String htmlContent;

        public EmailParameters(String to, String subject, String htmlContent) {
            this.to = to;
            this.subject = subject;
            this.htmlContent = htmlContent;
        }
    }
}
