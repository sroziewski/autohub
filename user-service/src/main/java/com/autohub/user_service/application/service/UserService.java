package com.autohub.user_service.application.service;

import com.autohub.user_service.domain.entity.RoleType;
import com.autohub.user_service.domain.entity.User;
import com.autohub.user_service.domain.entity.UserStatus;
import com.autohub.user_service.presentation.dto.auth.TwoFactorSetupResponse;
import com.autohub.user_service.presentation.dto.user.RegisterUserRequest;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for user-related operations.
 */
public interface UserService {

    /**
     * Registers a new user
     *
     * @param request Registration request data
     * @return The created user
     */
    User registerUser(RegisterUserRequest request);

    /**
     * Retrieves a user by their ID
     *
     * @param id User's unique identifier
     * @return Optional containing the user if found
     */
    Optional<User> findById(UUID id);

    /**
     * Retrieves a user by their email
     *
     * @param email User's email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Updates an existing user's profile information
     *
     * @param userId User's unique identifier
     * @param request Updated profile data
     * @return The updated user
     */
    User updateProfile(UUID userId, RegisterUserRequest request);

    /**
     * Verifies a user's email using a verification token
     *
     * @param token The verification token
     * @return true if verification was successful
     */
    boolean verifyEmail(String token);

    /**
     * Deactivates a user account
     *
     * @param userId User's unique identifier
     * @return The deactivated user
     */
    User deactivateAccount(UUID userId);

    /**
     * Reactivates a previously deactivated user account
     *
     * @param userId User's unique identifier
     * @return The reactivated user
     */
    User reactivateAccount(UUID userId);

    /**
     * Updates a user's status (ACTIVE, INACTIVE, BANNED)
     *
     * @param userId User's unique identifier
     * @param status New status
     * @return The updated user
     */
    User updateUserStatus(UUID userId, UserStatus status);

    /**
     * Initiates the password reset process
     *
     * @param email User's email address
     * @return true if reset was initiated successfully
     */
    boolean initiatePasswordReset(String email);

    /**
     * Completes the password reset process
     *
     * @param token Reset token
     * @param newPassword New password
     * @return true if password was reset successfully
     */
    boolean completePasswordReset(String token, String newPassword);

    /**
     * Grants a specific role to a user
     *
     * @param userId User's unique identifier
     * @param roleType Role to grant
     * @return The updated user
     */
    User grantRole(UUID userId, RoleType roleType);

    /**
     * Revokes a specific role from a user
     *
     * @param userId User's unique identifier
     * @param roleType Role to revoke
     * @return The updated user
     */
    User revokeRole(UUID userId, RoleType roleType);

    /**
     * Records a user login
     *
     * @param userId User's unique identifier
     */
    void recordLogin(UUID userId);

    /**
     * Initiates two-factor authentication setup for a user
     *
     * @param userId User's unique identifier
     * @return Setup information including secret and QR code URI
     */
    TwoFactorSetupResponse initiateTwoFactorSetup(UUID userId);

    /**
     * Completes two-factor authentication setup for a user
     *
     * @param userId User's unique identifier
     * @param secret The TOTP secret key
     * @param code The verification code from the authenticator app
     * @return true if setup was completed successfully
     */
    boolean completeTwoFactorSetup(UUID userId, String secret, String code);

    /**
     * Disables two-factor authentication for a user
     *
     * @param userId User's unique identifier
     * @return true if 2FA was disabled successfully
     */
    boolean disableTwoFactorAuth(UUID userId);

    /**
     * Verifies a two-factor authentication code
     *
     * @param userId User's unique identifier
     * @param code The verification code
     * @param isBackupCode Whether this is a backup code
     * @return true if the code is valid
     */
    boolean verifyTwoFactorCode(UUID userId, String code, boolean isBackupCode);

    /**
     * Records a failed login attempt for a user and locks the account if necessary
     *
     * @param email User's email address
     * @return true if the account was locked as a result of this failed attempt
     */
    boolean recordFailedLoginAttempt(String email);

    /**
     * Checks if a user account is locked due to too many failed login attempts
     *
     * @param email User's email address
     * @return true if the account is locked
     */
    boolean isAccountLocked(String email);
}
