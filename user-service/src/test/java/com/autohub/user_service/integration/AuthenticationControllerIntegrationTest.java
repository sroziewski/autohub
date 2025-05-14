package com.autohub.user_service.integration;

import com.autohub.user_service.infrastructure.persistence.entity.RoleType;
import com.autohub.user_service.infrastructure.persistence.entity.UserEntity;
import com.autohub.user_service.infrastructure.persistence.entity.UserStatus;
import com.autohub.user_service.infrastructure.persistence.repository.UserRepository;
import com.autohub.user_service.presentation.dto.auth.AuthenticationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class AuthenticationControllerIntegrationTest {

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


    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("autohub_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @DynamicPropertySource
    static void flywayProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.baseline-on-migrate", () -> "true");
        registry.add("spring.flyway.schemas", () -> "autohub");
        registry.add("spring.flyway.default-schema", () -> "autohub");
        registry.add("spring.flyway.clean-disabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test1234!";
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // Create a test user with encoded password
        String encodedPassword = passwordEncoder.encode(TEST_PASSWORD);

        testUser = new UserEntity();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(encodedPassword);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setVerified(true);
        testUser.addRole(RoleType.USER);

        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void loginWithValidCredentials_ShouldReturnJwtToken() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest(TEST_EMAIL, TEST_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/login")  // Make sure path is correct with context-path
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt", notNullValue()));
    }

    @Test
    public void loginWithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest(TEST_EMAIL, "wrongPassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void loginWithNonExistentUser_ShouldReturnUnauthorized() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest("nonexistent@example.com", TEST_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void loginWithInactiveUser_ShouldReturnUnauthorized() throws Exception {
        // Given - Update user to inactive
        testUser.setStatus(UserStatus.INACTIVE);
        userRepository.save(testUser);

        AuthenticationRequest request = new AuthenticationRequest(TEST_EMAIL, TEST_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void loginWithBannedUser_ShouldReturnUnauthorized() throws Exception {
        // Given - Update user to banned
        testUser.setStatus(UserStatus.BANNED);
        userRepository.save(testUser);

        AuthenticationRequest request = new AuthenticationRequest(TEST_EMAIL, TEST_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void loginWithInvalidEmailFormat_ShouldReturnBadRequest() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest("not-an-email", TEST_PASSWORD);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void loginWithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest(TEST_EMAIL, "");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void loginShouldUpdateLastLoginTimestamp() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest(TEST_EMAIL, TEST_PASSWORD);

        // When
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        UserEntity updatedUser = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        assert updatedUser.getLastLoginAt() != null;
    }
}
