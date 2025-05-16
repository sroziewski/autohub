package com.autohub.user_service.presentation.controller;

import com.autohub.user_service.application.service.SessionService;
import com.autohub.user_service.domain.entity.Session;
import com.autohub.user_service.domain.exception.ResourceNotFoundException;
import com.autohub.user_service.presentation.dto.common.ApiResponse;
import com.autohub.user_service.presentation.dto.session.SessionResponse;
import com.autohub.user_service.presentation.mapper.SessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final SessionService sessionService;
    private final SessionMapper sessionMapper;

    /**
     * Get all active sessions for the current user
     *
     * @param userDetails Authenticated user details
     * @param sessionId Current session ID (from request attribute)
     * @return List of active sessions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getActiveSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestAttribute(name = "sessionId", required = false) UUID sessionId) {

        log.info("Getting active sessions for user: {}", userDetails.getUsername());
        UUID userId = UUID.fromString(userDetails.getUsername());

        List<Session> sessions = sessionService.findActiveSessionsByUserId(userId);
        List<SessionResponse> sessionResponses = sessions.stream()
                .map(session -> sessionMapper.toResponseWithCurrentFlag(session, sessionId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(sessionResponses));
    }

    /**
     * Get a specific session by ID
     *
     * @param userDetails Authenticated user details
     * @param id Session ID
     * @param sessionId Current session ID (from request attribute)
     * @return The session if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SessionResponse>> getSessionById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestAttribute(name = "sessionId", required = false) UUID sessionId) {

        log.info("Getting session {} for user: {}", id, userDetails.getUsername());

        Session session = sessionService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        // Verify the session belongs to the authenticated user
        UUID userId = UUID.fromString(userDetails.getUsername());
        if (!session.getUserId().equals(userId)) {
            log.warn("User {} attempted to access session {} belonging to user {}", 
                    userDetails.getUsername(), id, session.getUserId());
            throw new ResourceNotFoundException("Session not found");
        }

        SessionResponse response = sessionMapper.toResponseWithCurrentFlag(session, sessionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Terminate a specific session
     *
     * @param userDetails Authenticated user details
     * @param id Session ID to terminate
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> terminateSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {

        log.info("Terminating session {} for user: {}", id, userDetails.getUsername());

        // First verify the session exists and belongs to the user
        Session session = sessionService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        UUID userId = UUID.fromString(userDetails.getUsername());
        if (!session.getUserId().equals(userId)) {
            log.warn("User {} attempted to terminate session {} belonging to user {}", 
                    userDetails.getUsername(), id, session.getUserId());
            throw new ResourceNotFoundException("Session not found");
        }

        boolean terminated = sessionService.terminateSession(id);
        if (terminated) {
            return ResponseEntity.ok(ApiResponse.success("Session terminated successfully"));
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Failed to terminate session", 400, "SESSION_TERMINATION_FAILED"));
        }
    }

    /**
     * Terminate all sessions except the current one
     *
     * @param userDetails Authenticated user details
     * @param sessionId Current session ID (from request attribute)
     * @return Success response with count of terminated sessions
     */
    @DeleteMapping
    public ResponseEntity<?> terminateOtherSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestAttribute(name = "sessionId", required = false) UUID sessionId) {

        log.info("Terminating other sessions for user: {}", userDetails.getUsername());

        if (sessionId == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Current session ID not found", 400, "SESSION_ID_MISSING"));
        }

        UUID userId = UUID.fromString(userDetails.getUsername());
        int count = sessionService.terminateOtherSessions(userId, sessionId);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Successfully terminated %d other sessions", count)));
    }
}
