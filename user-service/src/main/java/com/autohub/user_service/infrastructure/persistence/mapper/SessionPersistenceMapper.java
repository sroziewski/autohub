package com.autohub.user_service.infrastructure.persistence.mapper;

import com.autohub.user_service.domain.entity.Session;
import com.autohub.user_service.infrastructure.persistence.entity.SessionEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SessionPersistenceMapper {

    SessionPersistenceMapper INSTANCE = Mappers.getMapper(SessionPersistenceMapper.class);

    /**
     * Maps a persistence entity to a domain model
     *
     * @param entity The SessionEntity to convert
     * @return The domain Session
     */
    Session toDomain(SessionEntity entity);

    /**
     * Maps a domain model to a persistence entity
     *
     * @param domain The domain Session to convert
     * @return The persistence SessionEntity
     */
    @Mapping(target = "createdAt", ignore = true) // Handled by @PrePersist
    SessionEntity toEntity(Session domain);
}
