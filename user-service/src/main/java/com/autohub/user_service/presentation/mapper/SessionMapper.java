package com.autohub.user_service.presentation.mapper;

import com.autohub.user_service.domain.entity.Session;
import com.autohub.user_service.presentation.dto.session.SessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    SessionMapper INSTANCE = Mappers.getMapper(SessionMapper.class);

    /**
     * Maps a Session domain entity to a SessionResponse DTO
     *
     * @param session The session to map
     * @return The mapped SessionResponse
     */
    @Mapping(target = "current", ignore = true)
    SessionResponse toResponse(Session session);

    /**
     * Maps a Session domain entity to a SessionResponse DTO with current session flag
     *
     * @param session The session to map
     * @param currentSessionId The ID of the current session
     * @return The mapped SessionResponse
     */
    @Mapping(target = "current", expression = "java(isCurrentSession(session.getId(), currentSessionId))")
    SessionResponse toResponseWithCurrentFlag(Session session, UUID currentSessionId);

    /**
     * Checks if the given session ID matches the current session ID
     *
     * @param sessionId The session ID to check
     * @param currentSessionId The current session ID
     * @return true if the session is the current session
     */
    @Named("isCurrentSession")
    default boolean isCurrentSession(UUID sessionId, UUID currentSessionId) {
        return sessionId != null && sessionId.equals(currentSessionId);
    }
}
