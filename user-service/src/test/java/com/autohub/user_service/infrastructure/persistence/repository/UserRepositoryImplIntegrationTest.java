package com.autohub.user_service.infrastructure.persistence.repository;

import com.autohub.user_service.domain.entity.RoleType;
import com.autohub.user_service.domain.entity.User;
import com.autohub.user_service.domain.entity.UserStatus;
import com.autohub.user_service.infrastructure.persistence.entity.UserEntity;
import com.autohub.user_service.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.autohub.user_service.infrastructure.persistence.repository.jpa.UserJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserPersistenceMapper.class)
public class UserRepositoryImplIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("autohub_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private UserPersistenceMapper mapper;

    private UserRepositoryImpl userRepository;

    private User testUser;
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
        userRepository = new UserRepositoryImpl(userJpaRepository, mapper);
        userJpaRepository.deleteAll();

        // Create a test user
        testUser = User.createNew("test@example.com", "encodedPassword")
                .updateProfile("John", "Middle", "Doe", "1234567890", LocalDate.of(1990, 1, 1))
                .addRole(RoleType.USER);

        // Save the user and get the ID
        testUser = userRepository.save(testUser);
        userId = testUser.getId();
    }

    @AfterEach
    void tearDown() {
        userJpaRepository.deleteAll();
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        // Act
        Optional<User> result = userRepository.findById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Act
        Optional<User> result = userRepository.findById(UUID.randomUUID());

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        // Act
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Act
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByVerificationToken_ShouldReturnUser_WhenTokenExists() {
        // Arrange
        String token = "verification-token";
        UserEntity userEntity = userJpaRepository.findById(userId).orElseThrow();
        userEntity.setVerificationToken(token);
        userJpaRepository.save(userEntity);

        // Act
        Optional<User> result = userRepository.findByVerificationToken(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
    }

    @Test
    void findByVerificationToken_ShouldReturnEmpty_WhenTokenDoesNotExist() {
        // Act
        Optional<User> result = userRepository.findByVerificationToken("nonexistent-token");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByResetPasswordToken_ShouldReturnUser_WhenTokenExists() {
        // Arrange
        String token = "reset-token";
        UserEntity userEntity = userJpaRepository.findById(userId).orElseThrow();
        userEntity.setResetPasswordToken(token);
        userJpaRepository.save(userEntity);

        // Act
        Optional<User> result = userRepository.findByResetPasswordToken(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
    }

    @Test
    void findByResetPasswordToken_ShouldReturnEmpty_WhenTokenDoesNotExist() {
        // Act
        Optional<User> result = userRepository.findByResetPasswordToken("nonexistent-token");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void save_ShouldCreateNewUser_WhenUserDoesNotExist() {
        // Arrange
        User newUser = User.createNew("new@example.com", "encodedPassword")
                .updateProfile("Jane", null, "Smith", "9876543210", LocalDate.of(1992, 2, 2))
                .addRole(RoleType.USER);

        // Act
        User savedUser = userRepository.save(newUser);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("new@example.com", savedUser.getEmail());
        assertEquals("Jane", savedUser.getFirstName());
        assertEquals("Smith", savedUser.getLastName());

        // Verify it's in the database
        assertTrue(userJpaRepository.findById(savedUser.getId()).isPresent());
    }

    @Test
    void save_ShouldUpdateExistingUser_WhenUserExists() {
        // Arrange
        User updatedUser = testUser.updateProfile("Jane", "Middle", "Smith", "9876543210", LocalDate.of(1992, 2, 2));

        // Act
        User savedUser = userRepository.save(updatedUser);

        // Assert
        assertEquals(userId, savedUser.getId());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("Jane", savedUser.getFirstName());
        assertEquals("Smith", savedUser.getLastName());

        // Verify it's updated in the database
        UserEntity entity = userJpaRepository.findById(userId).orElseThrow();
        assertEquals("Jane", entity.getFirstName());
        assertEquals("Smith", entity.getLastName());
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // Act
        boolean result = userRepository.existsByEmail("test@example.com");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // Act
        boolean result = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result);
    }

    @Test
    void findAll_ShouldReturnPageOfUsers() {
        // Arrange
        User secondUser = User.createNew("second@example.com", "encodedPassword")
                .updateProfile("Second", null, "User", "5555555555", LocalDate.of(1985, 5, 5))
                .addRole(RoleType.USER);
        userRepository.save(secondUser);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<User> result = userRepository.findAll(pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(u -> u.getEmail().equals("test@example.com")));
        assertTrue(result.getContent().stream().anyMatch(u -> u.getEmail().equals("second@example.com")));
    }
}
