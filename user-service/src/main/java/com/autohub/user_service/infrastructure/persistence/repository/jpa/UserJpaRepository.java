package com.autohub.user_service.infrastructure.persistence.repository.jpa;

import com.autohub.user_service.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByVerificationToken(String token);

    Optional<UserEntity> findByResetPasswordToken(String token);

    boolean existsByEmail(String email);
}
