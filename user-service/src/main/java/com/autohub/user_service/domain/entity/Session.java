package com.autohub.user_service.domain.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing a User Session in the business context,
 * independent of persistence concerns.
 */
@Getter
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode(of = {"id"})
public class Session {
    private final UUID id;
    private final UUID userId;
    private final String ipAddress;
    private final String userAgent;
    private final String deviceInfo;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastActiveAt;
    private final LocalDateTime expiresAt;
    private final boolean active;

    /**
     * Creates a new session for a user
     *
     * @param userId     User's unique identifier
     * @param ipAddress  IP address of the client
     * @param userAgent  User agent string from the client
     * @param deviceInfo Additional device information
     * @param expiryHours Number of hours until the session expires
     * @return A new session domain object
     */
    public static Session createNew(UUID userId, String ipAddress, String userAgent, String deviceInfo, int expiryHours) {
        LocalDateTime now = LocalDateTime.now();
        return Session.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceInfo(deviceInfo)
                .createdAt(now)
                .lastActiveAt(now)
                .expiresAt(now.plusHours(expiryHours))
                .active(true)
                .build();
    }

    /**
     * Updates the last active time of the session
     *
     * @return A new session domain object with updated last active time
     */
    public Session updateLastActive() {
        return toBuilder()
                .lastActiveAt(LocalDateTime.now())
                .build();
    }

    /**
     * Terminates the session
     *
     * @return A new session domain object with active set to false
     */
    public Session terminate() {
        return toBuilder()
                .active(false)
                .build();
    }

    /**
     * Checks if the session is expired
     *
     * @return true if the session is expired
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Checks if the session is valid (active and not expired)
     *
     * @return true if the session is valid
     */
    public boolean isValid() {
        return active && !isExpired();
    }
}
