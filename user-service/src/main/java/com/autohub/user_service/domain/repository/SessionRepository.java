package com.autohub.user_service.domain.repository;

import com.autohub.user_service.domain.entity.Session;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Session entity operations.
 */
public interface SessionRepository {

    /**
     * Save a session
     *
     * @param session The session to save
     * @return The saved session
     */
    Session save(Session session);

    /**
     * Find a session by its ID
     *
     * @param id Session ID
     * @return Optional containing the session if found
     */
    Optional<Session> findById(UUID id);

    /**
     * Find all active sessions for a user
     *
     * @param userId User ID
     * @return List of active sessions
     */
    List<Session> findActiveSessionsByUserId(UUID userId);

    /**
     * Find all sessions for a user
     *
     * @param userId User ID
     * @return List of all sessions
     */
    List<Session> findAllByUserId(UUID userId);

    /**
     * Delete a session
     *
     * @param id Session ID
     */
    void deleteById(UUID id);

    /**
     * Delete all sessions for a user
     *
     * @param userId User ID
     */
    void deleteAllByUserId(UUID userId);

    /**
     * Delete all expired sessions
     */
    void deleteExpiredSessions();
}
