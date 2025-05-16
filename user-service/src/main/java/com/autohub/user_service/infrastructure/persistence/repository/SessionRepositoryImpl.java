package com.autohub.user_service.infrastructure.persistence.repository;

import com.autohub.user_service.domain.entity.Session;
import com.autohub.user_service.domain.repository.SessionRepository;
import com.autohub.user_service.infrastructure.persistence.mapper.SessionPersistenceMapper;
import com.autohub.user_service.infrastructure.persistence.repository.jpa.SessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SessionRepositoryImpl implements SessionRepository {

    private final SessionJpaRepository sessionJpaRepository;
    private final SessionPersistenceMapper mapper;

    @Override
    public Session save(Session session) {
        return mapper.toDomain(sessionJpaRepository.save(mapper.toEntity(session)));
    }

    @Override
    public Optional<Session> findById(UUID id) {
        return sessionJpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Session> findActiveSessionsByUserId(UUID userId) {
        return sessionJpaRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Session> findAllByUserId(UUID userId) {
        return sessionJpaRepository.findByUserId(userId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        sessionJpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAllByUserId(UUID userId) {
        sessionJpaRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteExpiredSessions() {
        sessionJpaRepository.deleteExpiredSessions(LocalDateTime.now());
    }
}
