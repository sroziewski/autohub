package com.autohub.user_service.presentation.controller;

import com.autohub.user_service.application.service.UserService;
import com.autohub.user_service.domain.entity.User;
import com.autohub.user_service.domain.entity.UserStatus;
import com.autohub.user_service.domain.exception.ResourceNotFoundException;
import com.autohub.user_service.presentation.dto.common.ApiResponse;
import com.autohub.user_service.presentation.dto.common.PageResponse;
import com.autohub.user_service.presentation.dto.user.RegisterUserRequest;
import com.autohub.user_service.presentation.dto.user.UserResponse;
import com.autohub.user_service.presentation.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        User newUser = userService.registerUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(userMapper.toResponse(newUser)));
    }

    /**
     * Get the current user's profile
     *
     * @param userDetails Authenticated user details
     * @return The user profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Getting current user profile for: {}", userDetails.getUsername());
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(userMapper.toResponse(user)));
    }

    /**
     * Get a user by ID
     *
     * @param id User ID
     * @return The user if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        log.info("Getting user by ID: {}", id);
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(userMapper.toResponse(user)));
    }

    /**
     * Update user profile
     *
     * @param userDetails Authenticated user details
     * @param request     User update data
     * @return The updated user
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RegisterUserRequest request) {

        log.info("Updating profile for user: {}", userDetails.getUsername());
        User user = userService.updateProfile(
                UUID.fromString(userDetails.getUsername()),
                request
        );

        return ResponseEntity.ok(ApiResponse.success(userMapper.toResponse(user)));
    }

    /**
     * Verify user email
     *
     * @param token Verification token
     * @return Success response
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        log.info("Verifying email with token: {}", token);
        boolean verified = userService.verifyEmail(token);

        if (verified) {
            return ResponseEntity.ok(ApiResponse.success("Email verified successfully"));
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Invalid or expired verification token", HttpStatus.BAD_REQUEST.value(), "INVALID_TOKEN"));
        }
    }

    /**
     * Deactivate user account
     *
     * @param userDetails Authenticated user details
     * @return Success response
     */
    @PostMapping("/me/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateAccount(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Deactivating account for user: {}", userDetails.getUsername());
        User user = userService.deactivateAccount(UUID.fromString(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success("Account deactivated successfully"));
    }

    /**
     * Reactivate user account
     *
     * @param userDetails Authenticated user details
     * @return Success response
     */
    @PostMapping("/me/reactivate")
    public ResponseEntity<ApiResponse<String>> reactivateAccount(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Reactivating account for user: {}", userDetails.getUsername());
        User user = userService.reactivateAccount(UUID.fromString(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.success("Account reactivated successfully"));
    }

    /**
     * Admin endpoint to change a user's status
     *
     * @param id     User ID
     * @param status New status
     * @return Success response
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateUserStatus(
            @PathVariable UUID id,
            @RequestParam UserStatus status) {

        log.info("Updating status to {} for user ID: {}", status, id);
        User user = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully"));
    }

    /**
     * Get all users with pagination
     *
     * @param page Page number (0-based)
     * @param size Page size
     * @param sort Sort field
     * @param direction Sort direction (ASC or DESC)
     * @return Paginated list of users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("Getting all users with pagination: page={}, size={}, sort={}, direction={}", 
                page, size, sort, direction);

        // Create pageable object with sorting
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // Get paginated users from service
        Page<User> userPage = userService.findAllUsers(pageable);

        // Map domain entities to DTOs
        Page<UserResponse> userResponsePage = userPage.map(userMapper::toResponse);

        // Create PageResponse from Page
        PageResponse<UserResponse> pageResponse = PageResponse.from(userResponsePage);

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }
}
