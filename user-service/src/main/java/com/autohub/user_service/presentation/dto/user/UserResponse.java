package com.autohub.user_service.presentation.dto.user;

import com.autohub.user_service.domain.entity.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String phone;
    private UserStatus status;
    private String firstName;
    private String secondName;
    private String lastName;
    private String fullName;
    private LocalDate birthDate;
    private LocalDateTime lastLoginAt;
    private boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> roles;
}
