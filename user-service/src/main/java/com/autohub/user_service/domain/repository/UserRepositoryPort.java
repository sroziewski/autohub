package com.autohub.user_service.domain.repository;

import com.autohub.user_service.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);

    User save(User user);

    Optional<User> findById(UUID id);
}
