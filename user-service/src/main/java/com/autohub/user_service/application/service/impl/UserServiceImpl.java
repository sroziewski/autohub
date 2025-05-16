package com.autohub.user_service.application.service.impl;

import com.autohub.user_service.application.exception.ResourceNotFoundException;
import com.autohub.user_service.application.service.UserService;
import com.autohub.user_service.domain.entity.RoleType;
import com.autohub.user_service.domain.entity.User;
import com.autohub.user_service.domain.entity.UserStatus;
import com.autohub.user_service.infrastructure.persistence.repository.UserRepositoryImpl;
import com.autohub.user_service.infrastructure.service.EmailService;
import com.autohub.user_service.presentation.dto.user.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the UserService interface.
 */
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserRepositoryImpl userRepositoryImpl;

    @Override
    @Transactional
    public User registerUser(RegisterUserRequest request) {
        // Check if email is already in use
        if (userRepositoryImpl.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("error.user.email_taken");
        }

        // Create new user with all information in a fluent style
        User user = User.createNew(request.getEmail(), passwordEncoder.encode(request.getPassword()))
                .updateProfile(
                        request.getFirstName(),
                        request.getSecondName(),
                        request.getLastName(),
                        request.getPhone(),
                        request.getBirthDate()
                )
                .addRole(RoleType.USER);

        // Save user via the repository that handles the mapping
        User savedUser = userRepositoryImpl.save(user);

        // Send verification email
        emailService.sendVerificationEmail(savedUser);

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepositoryImpl.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepositoryImpl.findByEmail(email);
    }

    @Override
    @Transactional
    public User updateProfile(UUID userId, RegisterUserRequest request) {
        User user = userRepositoryImpl.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not_found"));

        User updatedUser = user.updateProfile(
                request.getFirstName(),
                request.getSecondName(),
                request.getLastName(),
                request.getPhone(),
                request.getBirthDate()
        );

        // If password was provided, update it
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            updatedUser = updatedUser.toBuilder()
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();
        }

        return userRepositoryImpl.save(updatedUser);
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        Optional<User> userOptional = userRepositoryImpl.findByVerificationToken(token);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        User verifiedUser = user.verify(token);

        // If no changes were made, verification failed
        if (verifiedUser == user) {
            return false;
        }

        userRepositoryImpl.save(verifiedUser);
        return true;
    }

    @Override
    @Transactional
    public User deactivateAccount(UUID userId) {
        User user = userRepositoryImpl.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not_found"));

        User deactivatedUser = user.deactivate();
        return userRepositoryImpl.save(deactivatedUser);
    }

    @Override
    @Transactional
    public User reactivateAccount(UUID userId) {
        User user = userRepositoryImpl.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not_found"));

        User activatedUser = user.activate();
        return userRepositoryImpl.save(activatedUser);
    }

    @Override
    @Transactional
    public User updateUserStatus(UUID userId, UserStatus status) {
        User user = userRepositoryImpl.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not_found"));

        User updatedUser;
        switch (status) {
            case ACTIVE:
                updatedUser = user.activate();
                break;
            case INACTIVE:
                updatedUser = user.deactivate();
                break;
            case BANNED:
                updatedUser = user.ban();
                break;
            default:
                return user; // No change
        }

        return userRepositoryImpl.save(updatedUser);
    }

    @Override
    @Transactional
    public boolean initiatePasswordReset(String email) {
        Optional<User> userOptional = userRepositoryImpl.findByEmail(email);

        if (userOptional.isEmpty()) {
            // Return true for security reasons (don't reveal if email exists)
            return true;
        }

        User user = userOptional.get();
        User userWithResetToken = user.initiatePasswordReset(24); // 24 hours expiry

        userRepositoryImpl.save(userWithResetToken);

        // Send password reset email
        emailService.sendPasswordResetEmail(userWithResetToken);

        return true;
    }

    @Override
    @Transactional
    public boolean completePasswordReset(String token, String newPassword) {
        Optional<User> userOptional = userRepositoryImpl.findByResetPasswordToken(token);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        if (!user.isResetTokenValid(token)) {
            return false;
        }

        User updatedUser = user.completePasswordReset(
                token,
                passwordEncoder.encode(newPassword)
        );

        // If no changes were made, reset failed
        if (updatedUser == user) {
            return false;
        }

        userRepositoryImpl.save(updatedUser);
        return true;
    }

    @Override
    @Transactional
    public User grantRole(UUID userId, RoleType roleType) {
        User user = userRepositoryImpl.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not_found"));

        User updatedUser = user.addRole(roleType);
        return userRepositoryImpl.save(updatedUser);
    }

    @Override
    @Transactional
    public User revokeRole(UUID userId, RoleType roleType) {
        User user = userRepositoryImpl.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not_found"));

        User updatedUser = user.removeRole(roleType);
        return userRepositoryImpl.save(updatedUser);
    }

    @Override
    @Transactional
    public void recordLogin(UUID userId) {
        User user = userRepositoryImpl.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not_found"));

        User updatedUser = user.recordLogin();
        userRepositoryImpl.save(updatedUser);
    }
}
