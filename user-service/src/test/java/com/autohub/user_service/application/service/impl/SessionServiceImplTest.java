package com.autohub.user_service.application.service.impl;

import com.autohub.user_service.domain.entity.Session;
import com.autohub.user_service.domain.repository.SessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SessionServiceImpl sessionService;

    private UUID userId;
    private UUID sessionId;
    private Session testSession;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        
        // Set session expiry hours via reflection
        ReflectionTestUtils.setField(sessionService, "sessionExpiryHours", 24);
        
        // Create a test session
        testSession = Session.createNew(userId, "192.168.1.1", "Mozilla/5.0", "Desktop", 24);
        
        // Mock common request headers
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
    }

    @Test
    void createSession_ShouldCreateAndReturnSession() {
        // Arrange
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        // Act
        Session result = sessionService.createSession(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(request).getHeader("User-Agent");
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void findById_ShouldReturnSession_WhenSessionExists() {
        // Arrange
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));

        // Act
        Optional<Session> result = sessionService.findById(sessionId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testSession, result.get());
        verify(sessionRepository).findById(sessionId);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenSessionDoesNotExist() {
        // Arrange
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // Act
        Optional<Session> result = sessionService.findById(sessionId);

        // Assert
        assertFalse(result.isPresent());
        verify(sessionRepository).findById(sessionId);
    }

    @Test
    void findActiveSessionsByUserId_ShouldReturnActiveSessions() {
        // Arrange
        List<Session> activeSessions = Arrays.asList(testSession);
        when(sessionRepository.findActiveSessionsByUserId(userId)).thenReturn(activeSessions);

        // Act
        List<Session> result = sessionService.findActiveSessionsByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSession, result.get(0));
        verify(sessionRepository).findActiveSessionsByUserId(userId);
    }

    @Test
    void findAllByUserId_ShouldReturnAllSessions() {
        // Arrange
        List<Session> allSessions = Arrays.asList(testSession);
        when(sessionRepository.findAllByUserId(userId)).thenReturn(allSessions);

        // Act
        List<Session> result = sessionService.findAllByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSession, result.get(0));
        verify(sessionRepository).findAllByUserId(userId);
    }

    @Test
    void terminateSession_ShouldReturnTrue_WhenSessionExists() {
        // Arrange
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        Session terminatedSession = testSession.terminate();
        when(sessionRepository.save(any(Session.class))).thenReturn(terminatedSession);

        // Act
        boolean result = sessionService.terminateSession(sessionId);

        // Assert
        assertTrue(result);
        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void terminateSession_ShouldReturnFalse_WhenSessionDoesNotExist() {
        // Arrange
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // Act
        boolean result = sessionService.terminateSession(sessionId);

        // Assert
        assertFalse(result);
        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void terminateAllSessions_ShouldTerminateAllActiveSessions() {
        // Arrange
        List<Session> activeSessions = Arrays.asList(testSession, testSession);
        when(sessionRepository.findActiveSessionsByUserId(userId)).thenReturn(activeSessions);
        Session terminatedSession = testSession.terminate();
        when(sessionRepository.save(any(Session.class))).thenReturn(terminatedSession);

        // Act
        int result = sessionService.terminateAllSessions(userId);

        // Assert
        assertEquals(2, result);
        verify(sessionRepository).findActiveSessionsByUserId(userId);
        verify(sessionRepository, times(2)).save(any(Session.class));
    }

    @Test
    void terminateOtherSessions_ShouldTerminateOtherActiveSessions() {
        // Arrange
        UUID currentSessionId = UUID.randomUUID();
        Session currentSession = testSession;
        Session otherSession = testSession;
        
        // Use reflection to set the session IDs
        ReflectionTestUtils.setField(currentSession, "id", currentSessionId);
        ReflectionTestUtils.setField(otherSession, "id", sessionId);
        
        List<Session> activeSessions = Arrays.asList(currentSession, otherSession);
        when(sessionRepository.findActiveSessionsByUserId(userId)).thenReturn(activeSessions);
        Session terminatedSession = otherSession.terminate();
        when(sessionRepository.save(any(Session.class))).thenReturn(terminatedSession);

        // Act
        int result = sessionService.terminateOtherSessions(userId, currentSessionId);

        // Assert
        assertEquals(1, result);
        verify(sessionRepository).findActiveSessionsByUserId(userId);
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void updateLastActive_ShouldUpdateAndReturnSession_WhenSessionExists() {
        // Arrange
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        Session updatedSession = testSession.updateLastActive();
        when(sessionRepository.save(any(Session.class))).thenReturn(updatedSession);

        // Act
        Optional<Session> result = sessionService.updateLastActive(sessionId);

        // Assert
        assertTrue(result.isPresent());
        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void updateLastActive_ShouldReturnEmpty_WhenSessionDoesNotExist() {
        // Arrange
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // Act
        Optional<Session> result = sessionService.updateLastActive(sessionId);

        // Assert
        assertFalse(result.isPresent());
        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void cleanupExpiredSessions_ShouldCallRepositoryMethod() {
        // Arrange
        doNothing().when(sessionRepository).deleteExpiredSessions();

        // Act
        int result = sessionService.cleanupExpiredSessions();

        // Assert
        assertEquals(0, result);
        verify(sessionRepository).deleteExpiredSessions();
    }

    @Test
    void extractDeviceInfo_ShouldReturnMobile_WhenUserAgentContainsMobile() {
        // Arrange
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 Mobile");

        // Act
        String result = sessionService.extractDeviceInfo(request);

        // Assert
        assertEquals("Mobile", result);
    }

    @Test
    void extractDeviceInfo_ShouldReturnTablet_WhenUserAgentContainsTablet() {
        // Arrange
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 iPad");

        // Act
        String result = sessionService.extractDeviceInfo(request);

        // Assert
        assertEquals("Tablet", result);
    }

    @Test
    void extractDeviceInfo_ShouldReturnDesktop_WhenUserAgentIsDesktop() {
        // Arrange
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 Chrome");

        // Act
        String result = sessionService.extractDeviceInfo(request);

        // Assert
        assertEquals("Desktop", result);
    }

    @Test
    void extractDeviceInfo_ShouldReturnUnknown_WhenUserAgentIsNull() {
        // Arrange
        when(request.getHeader("User-Agent")).thenReturn(null);

        // Act
        String result = sessionService.extractDeviceInfo(request);

        // Assert
        assertEquals("Unknown", result);
    }

    @Test
    void getClientIp_ShouldReturnXForwardedFor_WhenHeaderIsPresent() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");

        // Act
        Session result = sessionService.createSession(userId, request);

        // Assert
        assertEquals("10.0.0.1", result.getIpAddress());
    }

    @Test
    void getClientIp_ShouldReturnRemoteAddr_WhenXForwardedForIsNotPresent() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Act
        Session result = sessionService.createSession(userId, request);

        // Assert
        assertEquals("192.168.1.1", result.getIpAddress());
    }
}
