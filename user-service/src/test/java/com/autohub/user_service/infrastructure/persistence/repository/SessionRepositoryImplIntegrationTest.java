package com.autohub.user_service.infrastructure.persistence.repository;

import com.autohub.user_service.domain.entity.Session;
import com.autohub.user_service.infrastructure.persistence.entity.SessionEntity;
import com.autohub.user_service.infrastructure.persistence.mapper.SessionPersistenceMapper;
import com.autohub.user_service.infrastructure.persistence.repository.jpa.SessionJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(SessionPersistenceMapper.class)
public class SessionRepositoryImplIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("autohub_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private SessionJpaRepository sessionJpaRepository;

    @Autowired
    private SessionPersistenceMapper mapper;

    private SessionRepositoryImpl sessionRepository;

    private Session testSession;
    private UUID sessionId;
    private UUID userId;

    @BeforeAll
    static void createSchema() {
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword())) {

            Statement statement = conn.createStatement();
            statement.execute("CREATE SCHEMA IF NOT EXISTS autohub");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create schema", e);
        }
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @BeforeEach
    void setUp() {
        sessionRepository = new SessionRepositoryImpl(sessionJpaRepository, mapper);
        sessionJpaRepository.deleteAll();

        userId = UUID.randomUUID();
        
        // Create a test session
        testSession = Session.createNew(userId, "192.168.1.1", "Mozilla/5.0", "Desktop", 24);
        
        // Save the session and get the ID
        testSession = sessionRepository.save(testSession);
        sessionId = testSession.getId();
    }

    @AfterEach
    void tearDown() {
        sessionJpaRepository.deleteAll();
    }

    @Test
    void save_ShouldCreateNewSession() {
        // Arrange
        Session newSession = Session.createNew(userId, "10.0.0.1", "Chrome", "Mobile", 24);

        // Act
        Session savedSession = sessionRepository.save(newSession);

        // Assert
        assertNotNull(savedSession.getId());
        assertEquals(userId, savedSession.getUserId());
        assertEquals("10.0.0.1", savedSession.getIpAddress());
        assertEquals("Chrome", savedSession.getUserAgent());
        assertEquals("Mobile", savedSession.getDeviceInfo());
        assertTrue(savedSession.isActive());

        // Verify it's in the database
        assertTrue(sessionJpaRepository.findById(savedSession.getId()).isPresent());
    }

    @Test
    void save_ShouldUpdateExistingSession() {
        // Arrange
        Session updatedSession = testSession.terminate();

        // Act
        Session savedSession = sessionRepository.save(updatedSession);

        // Assert
        assertEquals(sessionId, savedSession.getId());
        assertEquals(userId, savedSession.getUserId());
        assertFalse(savedSession.isActive());

        // Verify it's updated in the database
        SessionEntity entity = sessionJpaRepository.findById(sessionId).orElseThrow();
        assertFalse(entity.isActive());
    }

    @Test
    void findById_ShouldReturnSession_WhenSessionExists() {
        // Act
        Optional<Session> result = sessionRepository.findById(sessionId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(sessionId, result.get().getId());
        assertEquals(userId, result.get().getUserId());
        assertEquals("192.168.1.1", result.get().getIpAddress());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenSessionDoesNotExist() {
        // Act
        Optional<Session> result = sessionRepository.findById(UUID.randomUUID());

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findActiveSessionsByUserId_ShouldReturnActiveSessions() {
        // Arrange
        Session secondSession = Session.createNew(userId, "10.0.0.1", "Chrome", "Mobile", 24);
        sessionRepository.save(secondSession);

        // Create an inactive session
        Session inactiveSession = Session.createNew(userId, "10.0.0.2", "Firefox", "Desktop", 24).terminate();
        sessionRepository.save(inactiveSession);

        // Act
        List<Session> result = sessionRepository.findActiveSessionsByUserId(userId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Session::isActive));
        assertTrue(result.stream().anyMatch(s -> s.getId().equals(sessionId)));
        assertTrue(result.stream().anyMatch(s -> s.getId().equals(secondSession.getId())));
    }

    @Test
    void findAllByUserId_ShouldReturnAllSessions() {
        // Arrange
        Session secondSession = Session.createNew(userId, "10.0.0.1", "Chrome", "Mobile", 24);
        sessionRepository.save(secondSession);

        // Create an inactive session
        Session inactiveSession = Session.createNew(userId, "10.0.0.2", "Firefox", "Desktop", 24).terminate();
        sessionRepository.save(inactiveSession);

        // Act
        List<Session> result = sessionRepository.findAllByUserId(userId);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(s -> s.getId().equals(sessionId)));
        assertTrue(result.stream().anyMatch(s -> s.getId().equals(secondSession.getId())));
        assertTrue(result.stream().anyMatch(s -> s.getId().equals(inactiveSession.getId())));
    }

    @Test
    void deleteById_ShouldDeleteSession() {
        // Act
        sessionRepository.deleteById(sessionId);

        // Assert
        assertFalse(sessionJpaRepository.findById(sessionId).isPresent());
    }

    @Test
    void deleteAllByUserId_ShouldDeleteAllSessionsForUser() {
        // Arrange
        Session secondSession = Session.createNew(userId, "10.0.0.1", "Chrome", "Mobile", 24);
        sessionRepository.save(secondSession);

        UUID otherUserId = UUID.randomUUID();
        Session otherUserSession = Session.createNew(otherUserId, "10.0.0.2", "Firefox", "Desktop", 24);
        sessionRepository.save(otherUserSession);

        // Act
        sessionRepository.deleteAllByUserId(userId);

        // Assert
        assertEquals(0, sessionJpaRepository.findByUserId(userId).size());
        assertEquals(1, sessionJpaRepository.findByUserId(otherUserId).size());
    }

    @Test
    void deleteExpiredSessions_ShouldDeleteExpiredSessions() {
        // Arrange
        // Create an expired session by manipulating the expiry time
        SessionEntity expiredEntity = sessionJpaRepository.findById(sessionId).orElseThrow();
        expiredEntity.setExpiresAt(LocalDateTime.now().minusHours(1));
        sessionJpaRepository.save(expiredEntity);

        // Create a non-expired session
        Session activeSession = Session.createNew(userId, "10.0.0.1", "Chrome", "Mobile", 24);
        sessionRepository.save(activeSession);

        // Act
        sessionRepository.deleteExpiredSessions();

        // Assert
        assertFalse(sessionJpaRepository.findById(sessionId).isPresent());
        assertTrue(sessionJpaRepository.findById(activeSession.getId()).isPresent());
    }
}
