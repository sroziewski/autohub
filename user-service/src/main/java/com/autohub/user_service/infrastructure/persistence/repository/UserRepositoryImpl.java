package com.autohub.user_service.infrastructure.persistence.repository;

import com.autohub.user_service.domain.entity.User;
import com.autohub.user_service.domain.repository.UserRepository;
import com.autohub.user_service.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.autohub.user_service.infrastructure.persistence.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByVerificationToken(String token) {
        return userJpaRepository.findByVerificationToken(token)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByResetPasswordToken(String token) {
        return userJpaRepository.findByResetPasswordToken(token)
                .map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(userJpaRepository.save(mapper.toEntity(user)));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    public Page<User> findAll(Pageable pageable) {
        return userJpaRepository.findAll(pageable)
                .map(mapper::toDomain);
    }
}
