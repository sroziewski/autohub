package com.autohub.user_service.infrastructure.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Message payload for email messages sent to the email queue.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage implements Serializable {
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> templateVariables;
    private String plainText;
    private int retryCount;
}
