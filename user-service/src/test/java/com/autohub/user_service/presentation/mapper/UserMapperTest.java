package com.autohub.user_service.presentation.mapper;

import com.autohub.user_service.domain.entity.User;
import com.autohub.user_service.domain.entity.UserStatus;
import com.autohub.user_service.presentation.dto.user.UserResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    /**
     * Tests for the UserMapper class, specifically the toResponse method.
     * The toResponse method is responsible for mapping a User entity to a UserResponse DTO.
     */

    @Test
    void testToResponseWithValidUser() {
        // Arrange
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        String email = "test@example.com";
        String phone = "123456789";
        UserStatus status = UserStatus.ACTIVE;
        String firstName = "John";
        String secondName = "Middle";
        String lastName = "Doe";
        String fullName = "John Middle Doe";
        LocalDate birthDate = LocalDate.of(1990, 5, 20);
        LocalDateTime lastLoginAt = LocalDateTime.of(2023, 10, 30, 12, 0);
        boolean verified = true;
        LocalDateTime createdAt = LocalDateTime.of(2022, 1, 10, 10, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2022, 12, 15, 15, 45);
        List<String> roleNames = List.of("ROLE_USER", "ROLE_ADMIN");

        User user = Mockito.mock(User.class);
        Mockito.when(user.getId()).thenReturn(userId);
        Mockito.when(user.getEmail()).thenReturn(email);
        Mockito.when(user.getPhone()).thenReturn(phone);
        Mockito.when(user.getStatus()).thenReturn(status);
        Mockito.when(user.getFirstName()).thenReturn(firstName);
        Mockito.when(user.getSecondName()).thenReturn(secondName);
        Mockito.when(user.getLastName()).thenReturn(lastName);
        Mockito.when(user.getFullName()).thenReturn(fullName);
        Mockito.when(user.getBirthDate()).thenReturn(birthDate);
        Mockito.when(user.getLastLoginAt()).thenReturn(lastLoginAt);
        Mockito.when(user.isVerified()).thenReturn(verified);
        Mockito.when(user.getCreatedAt()).thenReturn(createdAt);
        Mockito.when(user.getUpdatedAt()).thenReturn(updatedAt);
        Mockito.when(user.getRoleNames()).thenReturn(roleNames);

        UserMapper userMapper = UserMapper.INSTANCE;

        // Act
        UserResponse response = userMapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals(email, response.getEmail());
        assertEquals(phone, response.getPhone());
        assertEquals(status, response.getStatus());
        assertEquals(firstName, response.getFirstName());
        assertEquals(secondName, response.getSecondName());
        assertEquals(lastName, response.getLastName());
        assertEquals(fullName, response.getFullName());
        assertEquals(birthDate, response.getBirthDate());
        assertEquals(lastLoginAt, response.getLastLoginAt());
        assertEquals(verified, response.isVerified());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
        assertEquals(roleNames, response.getRoles());
    }

    @Test
    void testToResponseWithNullUser() {
        // Arrange
        UserMapper userMapper = UserMapper.INSTANCE;

        // Act
        UserResponse response = userMapper.toResponse(null);

        // Assert
        assertNull(response);
    }
}
