package com.autohub.user_service.domain.repository;

import com.autohub.user_service.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByResetPasswordToken(String token);
    User save(User user);
    boolean existsByEmail(String email);

}
