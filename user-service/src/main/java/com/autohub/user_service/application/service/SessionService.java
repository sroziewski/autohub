package com.autohub.user_service.application.service;

import com.autohub.user_service.domain.entity.Session;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for session-related operations.
 */
public interface SessionService {

    /**
     * Creates a new session for a user
     *
     * @param userId User's unique identifier
     * @param request HTTP request containing client information
     * @return The created session
     */
    Session createSession(UUID userId, HttpServletRequest request);

    /**
     * Retrieves a session by its ID
     *
     * @param id Session's unique identifier
     * @return Optional containing the session if found
     */
    Optional<Session> findById(UUID id);

    /**
     * Retrieves all active sessions for a user
     *
     * @param userId User's unique identifier
     * @return List of active sessions
     */
    List<Session> findActiveSessionsByUserId(UUID userId);

    /**
     * Retrieves all sessions for a user
     *
     * @param userId User's unique identifier
     * @return List of all sessions
     */
    List<Session> findAllByUserId(UUID userId);

    /**
     * Terminates a session
     *
     * @param id Session's unique identifier
     * @return true if the session was terminated successfully
     */
    boolean terminateSession(UUID id);

    /**
     * Terminates all sessions for a user
     *
     * @param userId User's unique identifier
     * @return Number of sessions terminated
     */
    int terminateAllSessions(UUID userId);

    /**
     * Terminates all sessions for a user except the current one
     *
     * @param userId User's unique identifier
     * @param currentSessionId Current session ID to exclude
     * @return Number of sessions terminated
     */
    int terminateOtherSessions(UUID userId, UUID currentSessionId);

    /**
     * Updates the last active time of a session
     *
     * @param id Session's unique identifier
     * @return The updated session
     */
    Optional<Session> updateLastActive(UUID id);

    /**
     * Cleans up expired sessions
     *
     * @return Number of sessions deleted
     */
    int cleanupExpiredSessions();

    /**
     * Extracts device information from the request
     *
     * @param request HTTP request
     * @return Device information string
     */
    String extractDeviceInfo(HttpServletRequest request);
}
