package com.autohub.user_service.infrastructure.service;

import com.autohub.user_service.domain.entity.User;
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

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.url}")
    private String appUrl;

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
     *
     * @param to Email recipient
     * @param subject Email subject
     * @param templateName Name of Thymeleaf template (without .html extension)
     * @param variables Variables to pass to template
     */
    private void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            // Prepare the context with variables
            Context context = new Context();
            variables.forEach(context::setVariable);

            // Process the template
            String htmlContent = templateEngine.process(templateName, context);

            // Create message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Send email
            mailSender.send(message);
            log.info("Email sent to {} with subject '{}'", to, subject);

        } catch (MessagingException e) {
            log.error("Failed to send email to {} with subject '{}': {}", to, subject, e.getMessage());
        }
    }
}
