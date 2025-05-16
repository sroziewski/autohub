package com.autohub.user_service.infrastructure.persistence.repository.jpa;

import com.autohub.user_service.infrastructure.persistence.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SessionJpaRepository extends JpaRepository<SessionEntity, UUID> {
    
    /**
     * Find all active sessions for a user
     *
     * @param userId User ID
     * @return List of active sessions
     */
    List<SessionEntity> findByUserIdAndActiveTrue(UUID userId);
    
    /**
     * Find all sessions for a user
     *
     * @param userId User ID
     * @return List of all sessions
     */
    List<SessionEntity> findByUserId(UUID userId);
    
    /**
     * Delete all sessions for a user
     *
     * @param userId User ID
     */
    void deleteByUserId(UUID userId);
    
    /**
     * Delete all expired sessions
     *
     * @param now Current time
     * @return Number of deleted sessions
     */
    @Modifying
    @Query("DELETE FROM SessionEntity s WHERE s.expiresAt < :now OR s.active = false")
    int deleteExpiredSessions(LocalDateTime now);
}
