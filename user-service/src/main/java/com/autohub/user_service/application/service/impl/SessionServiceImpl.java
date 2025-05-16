package com.autohub.user_service.application.service.impl;

import com.autohub.user_service.application.service.SessionService;
import com.autohub.user_service.domain.entity.Session;
import com.autohub.user_service.domain.repository.SessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    @Value("${app.session.expiry-hours:24}")
    private int sessionExpiryHours;

    @Override
    public Session createSession(UUID userId, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String deviceInfo = extractDeviceInfo(request);

        log.info("Creating new session for user {} from IP {} with device {}", userId, ipAddress, deviceInfo);
        
        Session session = Session.createNew(userId, ipAddress, userAgent, deviceInfo, sessionExpiryHours);
        return sessionRepository.save(session);
    }

    @Override
    public Optional<Session> findById(UUID id) {
        return sessionRepository.findById(id);
    }

    @Override
    public List<Session> findActiveSessionsByUserId(UUID userId) {
        return sessionRepository.findActiveSessionsByUserId(userId);
    }

    @Override
    public List<Session> findAllByUserId(UUID userId) {
        return sessionRepository.findAllByUserId(userId);
    }

    @Override
    @Transactional
    public boolean terminateSession(UUID id) {
        Optional<Session> sessionOpt = sessionRepository.findById(id);
        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            Session terminatedSession = session.terminate();
            sessionRepository.save(terminatedSession);
            log.info("Session {} terminated", id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public int terminateAllSessions(UUID userId) {
        List<Session> activeSessions = sessionRepository.findActiveSessionsByUserId(userId);
        int count = 0;
        
        for (Session session : activeSessions) {
            Session terminatedSession = session.terminate();
            sessionRepository.save(terminatedSession);
            count++;
        }
        
        log.info("Terminated {} sessions for user {}", count, userId);
        return count;
    }

    @Override
    @Transactional
    public int terminateOtherSessions(UUID userId, UUID currentSessionId) {
        List<Session> activeSessions = sessionRepository.findActiveSessionsByUserId(userId);
        int count = 0;
        
        for (Session session : activeSessions) {
            if (!session.getId().equals(currentSessionId)) {
                Session terminatedSession = session.terminate();
                sessionRepository.save(terminatedSession);
                count++;
            }
        }
        
        log.info("Terminated {} other sessions for user {}", count, userId);
        return count;
    }

    @Override
    @Transactional
    public Optional<Session> updateLastActive(UUID id) {
        Optional<Session> sessionOpt = sessionRepository.findById(id);
        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            Session updatedSession = session.updateLastActive();
            return Optional.of(sessionRepository.save(updatedSession));
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    @Scheduled(cron = "${app.session.cleanup-cron:0 0 * * * *}") // Default: Every hour
    public int cleanupExpiredSessions() {
        log.info("Running scheduled cleanup of expired sessions");
        sessionRepository.deleteExpiredSessions();
        // Since the actual deletion is done at the database level, we don't have a count
        // We could implement a count in the future if needed
        return 0;
    }

    @Override
    public String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown";
        }
        
        // Simple device detection based on User-Agent
        if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
            return "Mobile";
        } else if (userAgent.contains("Tablet") || userAgent.contains("iPad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    /**
     * Gets the client's IP address from the request, handling proxies
     *
     * @param request The HTTP request
     * @return The client's IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, the first one is the client
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
