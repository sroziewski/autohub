package com.autohub.user_service.infrastructure.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Message payload for audit log messages sent to the audit log queue.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogMessage implements Serializable {
    private String userId;
    private String action;
    private String resourceType;
    private String resourceId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private String details;
    private boolean success;
}
