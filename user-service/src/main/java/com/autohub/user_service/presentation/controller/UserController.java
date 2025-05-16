package com.autohub.user_service.presentation.controller;

import com.autohub.user_service.application.service.UserService;
import com.autohub.user_service.domain.entity.User;
import com.autohub.user_service.domain.entity.UserStatus;
import com.autohub.user_service.presentation.dto.user.RegisterUserRequest;
import com.autohub.user_service.presentation.dto.user.UserResponse;
import com.autohub.user_service.presentation.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Register a new user
     *
     * @param request User registration data
     * @return The created user
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        User newUser = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(newUser));
    }

    /**
     * Get the current user's profile
     *
     * @param userDetails Authenticated user details
     * @return The user profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    /**
     * Get a user by ID
     *
     * @param id User ID
     * @return The user if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    /**
     * Update user profile
     *
     * @param userDetails Authenticated user details
     * @param request     User update data
     * @return The updated user
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RegisterUserRequest request) {

        User user = userService.updateProfile(
                UUID.fromString(userDetails.getUsername()),
                request
        );

        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    /**
     * Verify user email
     *
     * @param token Verification token
     * @return Success response
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean verified = userService.verifyEmail(token);
        if (verified) {
            return ResponseEntity.ok("Email verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired verification token");
        }
    }

    /**
     * Deactivate user account
     *
     * @param userDetails Authenticated user details
     * @return Success response
     */
    @PostMapping("/me/deactivate")
    public ResponseEntity<String> deactivateAccount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.deactivateAccount(UUID.fromString(userDetails.getUsername()));
        return ResponseEntity.ok("Account deactivated successfully");
    }

    /**
     * Reactivate user account
     *
     * @param userDetails Authenticated user details
     * @return Success response
     */
    @PostMapping("/me/reactivate")
    public ResponseEntity<String> reactivateAccount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.reactivateAccount(UUID.fromString(userDetails.getUsername()));
        return ResponseEntity.ok("Account reactivated successfully");
    }

    /**
     * Admin endpoint to change a user's status
     *
     * @param id     User ID
     * @param status New status
     * @return Success response
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable UUID id,
            @RequestParam UserStatus status) {

        User user = userService.updateUserStatus(id, status);
        return ResponseEntity.ok("User status updated successfully");
    }
}

/**
 * Exception for resource not found scenarios
 */
class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
