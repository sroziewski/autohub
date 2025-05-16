package com.autohub.user_service.infrastructure.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Message payload for notification messages sent to the notification queue.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    private String userId;
    private String type;
    private String title;
    private String message;
    private Map<String, Object> data;
    private LocalDateTime timestamp;
    private boolean read;
    private String redirectUrl;
    private int priority;
}
