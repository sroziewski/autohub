package com.autohub.user_service.application.service.impl;

import com.autohub.user_service.application.exception.ResourceNotFoundException;
import com.autohub.user_service.application.service.TwoFactorAuthService;
import com.autohub.user_service.domain.entity.RoleType;
import com.autohub.user_service.domain.entity.User;
import com.autohub.user_service.domain.entity.UserStatus;
import com.autohub.user_service.infrastructure.persistence.repository.UserRepositoryImpl;
import com.autohub.user_service.infrastructure.service.EmailService;
import com.autohub.user_service.presentation.dto.auth.TwoFactorSetupResponse;
import com.autohub.user_service.presentation.dto.user.RegisterUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepositoryImpl userRepositoryImpl;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UUID userId;
    private RegisterUserRequest registerUserRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Create a test user
        testUser = User.createNew("test@example.com", "encodedPassword")
                .updateProfile("John", "Middle", "Doe", "1234567890", LocalDate.of(1990, 1, 1))
                .addRole(RoleType.USER);

        // Create a registration request
        registerUserRequest = RegisterUserRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .firstName("John")
                .secondName("Middle")
                .lastName("Doe")
                .phone("1234567890")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void registerUser_ShouldCreateAndReturnUser() {
        // Arrange
        when(userRepositoryImpl.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepositoryImpl.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendVerificationEmail(any(User.class));

        // Act
        User result = userService.registerUser(registerUserRequest);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepositoryImpl).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("Password123!");
        verify(userRepositoryImpl).save(any(User.class));
        verify(emailService).sendVerificationEmail(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepositoryImpl.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(registerUserRequest));
        verify(userRepositoryImpl).existsByEmail("test@example.com");
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepositoryImpl).findById(userId);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findById(userId);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepositoryImpl).findById(userId);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        // Arrange
        String email = "test@example.com";
        when(userRepositoryImpl.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepositoryImpl).findByEmail(email);
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepositoryImpl.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepositoryImpl).findByEmail(email);
    }

    @Test
    void updateProfile_ShouldUpdateAndReturnUser() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepositoryImpl.save(any(User.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        RegisterUserRequest updateRequest = RegisterUserRequest.builder()
                .email("test@example.com")
                .password("NewPassword123!")
                .firstName("Jane")
                .secondName("Middle")
                .lastName("Smith")
                .phone("9876543210")
                .birthDate(LocalDate.of(1992, 2, 2))
                .build();

        // Act
        User result = userService.updateProfile(userId, updateRequest);

        // Assert
        assertNotNull(result);
        verify(userRepositoryImpl).findById(userId);
        verify(passwordEncoder).encode("NewPassword123!");
        verify(userRepositoryImpl).save(any(User.class));
    }

    @Test
    void updateProfile_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateProfile(userId, registerUserRequest));
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void verifyEmail_ShouldReturnTrue_WhenTokenIsValid() {
        // Arrange
        String token = "valid-token";
        User verifiedUser = testUser.verify(token); // Assuming this method exists and works correctly
        when(userRepositoryImpl.findByVerificationToken(token)).thenReturn(Optional.of(testUser));
        when(userRepositoryImpl.save(any(User.class))).thenReturn(verifiedUser);

        // Act
        boolean result = userService.verifyEmail(token);

        // Assert
        assertTrue(result);
        verify(userRepositoryImpl).findByVerificationToken(token);
        verify(userRepositoryImpl).save(any(User.class));
    }

    @Test
    void verifyEmail_ShouldReturnFalse_WhenTokenIsInvalid() {
        // Arrange
        String token = "invalid-token";
        when(userRepositoryImpl.findByVerificationToken(token)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.verifyEmail(token);

        // Assert
        assertFalse(result);
        verify(userRepositoryImpl).findByVerificationToken(token);
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void deactivateAccount_ShouldDeactivateAndReturnUser() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        User deactivatedUser = testUser.deactivate(); // Assuming this method exists and works correctly
        when(userRepositoryImpl.save(any(User.class))).thenReturn(deactivatedUser);

        // Act
        User result = userService.deactivateAccount(userId);

        // Assert
        assertNotNull(result);
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl).save(any(User.class));
    }

    @Test
    void deactivateAccount_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.deactivateAccount(userId));
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void reactivateAccount_ShouldReactivateAndReturnUser() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        User activatedUser = testUser.activate(); // Assuming this method exists and works correctly
        when(userRepositoryImpl.save(any(User.class))).thenReturn(activatedUser);

        // Act
        User result = userService.reactivateAccount(userId);

        // Assert
        assertNotNull(result);
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl).save(any(User.class));
    }

    @Test
    void reactivateAccount_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.reactivateAccount(userId));
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void updateUserStatus_ShouldUpdateStatusAndReturnUser() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        User updatedUser = testUser.ban(); // Assuming this method exists and works correctly
        when(userRepositoryImpl.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.updateUserStatus(userId, UserStatus.BANNED);

        // Assert
        assertNotNull(result);
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl).save(any(User.class));
    }

    @Test
    void updateUserStatus_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserStatus(userId, UserStatus.BANNED));
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void initiatePasswordReset_ShouldReturnTrue_WhenUserExists() {
        // Arrange
        String email = "test@example.com";
        User userWithResetToken = testUser.initiatePasswordReset(24); // Assuming this method exists and works correctly
        when(userRepositoryImpl.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userRepositoryImpl.save(any(User.class))).thenReturn(userWithResetToken);
        doNothing().when(emailService).sendPasswordResetEmail(any(User.class));

        // Act
        boolean result = userService.initiatePasswordReset(email);

        // Assert
        assertTrue(result);
        verify(userRepositoryImpl).findByEmail(email);
        verify(userRepositoryImpl).save(any(User.class));
        verify(emailService).sendPasswordResetEmail(any(User.class));
    }

    @Test
    void initiatePasswordReset_ShouldReturnTrue_WhenUserDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepositoryImpl.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.initiatePasswordReset(email);

        // Assert
        assertTrue(result); // Should return true for security reasons
        verify(userRepositoryImpl).findByEmail(email);
        verify(userRepositoryImpl, never()).save(any(User.class));
        verify(emailService, never()).sendPasswordResetEmail(any(User.class));
    }

    @Test
    void completePasswordReset_ShouldReturnTrue_WhenTokenIsValid() {
        // Arrange
        String token = "valid-token";
        String newPassword = "NewPassword123!";
        User updatedUser = testUser.completePasswordReset(token, "encodedNewPassword"); // Assuming this method exists and works correctly
        when(userRepositoryImpl.findByResetPasswordToken(token)).thenReturn(Optional.of(testUser));
        when(testUser.isResetTokenValid(token)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepositoryImpl.save(any(User.class))).thenReturn(updatedUser);

        // Act
        boolean result = userService.completePasswordReset(token, newPassword);

        // Assert
        assertTrue(result);
        verify(userRepositoryImpl).findByResetPasswordToken(token);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepositoryImpl).save(any(User.class));
    }

    @Test
    void completePasswordReset_ShouldReturnFalse_WhenTokenIsInvalid() {
        // Arrange
        String token = "invalid-token";
        String newPassword = "NewPassword123!";
        when(userRepositoryImpl.findByResetPasswordToken(token)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.completePasswordReset(token, newPassword);

        // Assert
        assertFalse(result);
        verify(userRepositoryImpl).findByResetPasswordToken(token);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void completePasswordReset_ShouldReturnFalse_WhenTokenIsExpired() {
        // Arrange
        String token = "expired-token";
        String newPassword = "NewPassword123!";
        when(userRepositoryImpl.findByResetPasswordToken(token)).thenReturn(Optional.of(testUser));
        when(testUser.isResetTokenValid(token)).thenReturn(false);

        // Act
        boolean result = userService.completePasswordReset(token, newPassword);

        // Assert
        assertFalse(result);
        verify(userRepositoryImpl).findByResetPasswordToken(token);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void grantRole_ShouldGrantRoleAndReturnUser() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        User updatedUser = testUser.addRole(RoleType.ADMIN); // Assuming this method exists and works correctly
        when(userRepositoryImpl.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.grantRole(userId, RoleType.ADMIN);

        // Assert
        assertNotNull(result);
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl).save(any(User.class));
    }

    @Test
    void grantRole_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.grantRole(userId, RoleType.ADMIN));
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void revokeRole_ShouldRevokeRoleAndReturnUser() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        User updatedUser = testUser.removeRole(RoleType.USER); // Assuming this method exists and works correctly
        when(userRepositoryImpl.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.revokeRole(userId, RoleType.USER);

        // Assert
        assertNotNull(result);
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl).save(any(User.class));
    }

    @Test
    void revokeRole_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.revokeRole(userId, RoleType.USER));
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void recordLogin_ShouldUpdateLastLoginTimestamp() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        User updatedUser = testUser.recordLogin(); // Assuming this method exists and works correctly
        when(userRepositoryImpl.save(any(User.class))).thenReturn(updatedUser);

        // Act
        userService.recordLogin(userId);

        // Assert
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl).save(any(User.class));
    }

    @Test
    void recordLogin_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.recordLogin(userId));
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void initiateTwoFactorSetup_ShouldReturnSetupResponse() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        when(twoFactorAuthService.generateTotpSecret()).thenReturn("secret123");
        when(twoFactorAuthService.generateQrCodeUri(anyString(), anyString(), anyString()))
                .thenReturn("qrcode-uri");
        when(twoFactorAuthService.generateBackupCodes()).thenReturn(List.of("code1", "code2"));

        // Act
        TwoFactorSetupResponse response = userService.initiateTwoFactorSetup(userId);

        // Assert
        assertNotNull(response);
        assertEquals("secret123", response.getSecret());
        assertEquals("qrcode-uri", response.getQrCodeUri());
        assertEquals(List.of("code1", "code2"), response.getBackupCodes());
        verify(userRepositoryImpl).findById(userId);
        verify(twoFactorAuthService).generateTotpSecret();
        verify(twoFactorAuthService).generateQrCodeUri("secret123", "test@example.com", "AutoHub");
        verify(twoFactorAuthService).generateBackupCodes();
    }

    @Test
    void initiateTwoFactorSetup_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.initiateTwoFactorSetup(userId));
        verify(userRepositoryImpl).findById(userId);
        verify(twoFactorAuthService, never()).generateTotpSecret();
    }

    @Test
    void completeTwoFactorSetup_ShouldReturnTrue_WhenCodeIsValid() {
        // Arrange
        String secret = "secret123";
        String code = "123456";
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        when(twoFactorAuthService.verifyTotp(secret, code)).thenReturn(true);
        when(twoFactorAuthService.generateBackupCodes()).thenReturn(List.of("code1", "code2"));
        User updatedUser = testUser.enableTwoFactorAuth(secret, List.of("code1", "code2")); // Assuming this method exists and works correctly
        when(userRepositoryImpl.save(any(User.class))).thenReturn(updatedUser);

        // Act
        boolean result = userService.completeTwoFactorSetup(userId, secret, code);

        // Assert
        assertTrue(result);
        verify(userRepositoryImpl).findById(userId);
        verify(twoFactorAuthService).verifyTotp(secret, code);
        verify(twoFactorAuthService).generateBackupCodes();
        verify(userRepositoryImpl).save(any(User.class));
    }

    @Test
    void completeTwoFactorSetup_ShouldReturnFalse_WhenCodeIsInvalid() {
        // Arrange
        String secret = "secret123";
        String code = "invalid";
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        when(twoFactorAuthService.verifyTotp(secret, code)).thenReturn(false);

        // Act
        boolean result = userService.completeTwoFactorSetup(userId, secret, code);

        // Assert
        assertFalse(result);
        verify(userRepositoryImpl).findById(userId);
        verify(twoFactorAuthService).verifyTotp(secret, code);
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void completeTwoFactorSetup_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String secret = "secret123";
        String code = "123456";
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.completeTwoFactorSetup(userId, secret, code));
        verify(userRepositoryImpl).findById(userId);
        verify(twoFactorAuthService, never()).verifyTotp(anyString(), anyString());
    }

    @Test
    void disableTwoFactorAuth_ShouldReturnTrue_WhenTwoFactorIsEnabled() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        when(testUser.isTwoFactorEnabled()).thenReturn(true);
        User updatedUser = testUser.disableTwoFactorAuth(); // Assuming this method exists and works correctly
        when(userRepositoryImpl.save(any(User.class))).thenReturn(updatedUser);

        // Act
        boolean result = userService.disableTwoFactorAuth(userId);

        // Assert
        assertTrue(result);
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl).save(any(User.class));
    }

    @Test
    void disableTwoFactorAuth_ShouldReturnTrue_WhenTwoFactorIsNotEnabled() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        when(testUser.isTwoFactorEnabled()).thenReturn(false);

        // Act
        boolean result = userService.disableTwoFactorAuth(userId);

        // Assert
        assertTrue(result);
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void disableTwoFactorAuth_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.disableTwoFactorAuth(userId));
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void verifyTwoFactorCode_ShouldReturnTrue_WhenTotpCodeIsValid() {
        // Arrange
        String code = "123456";
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        when(testUser.isTwoFactorEnabled()).thenReturn(true);
        when(testUser.getTwoFactorSecret()).thenReturn("secret123");
        when(twoFactorAuthService.verifyTotp("secret123", code)).thenReturn(true);

        // Act
        boolean result = userService.verifyTwoFactorCode(userId, code, false);

        // Assert
        assertTrue(result);
        verify(userRepositoryImpl).findById(userId);
        verify(twoFactorAuthService).verifyTotp("secret123", code);
    }

    @Test
    void verifyTwoFactorCode_ShouldReturnTrue_WhenBackupCodeIsValid() {
        // Arrange
        String code = "backup123";
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        when(testUser.isTwoFactorEnabled()).thenReturn(true);
        when(testUser.getBackupCodes()).thenReturn(List.of("backup123", "backup456"));
        User updatedUser = testUser.useBackupCode(code); // Assuming this method exists and works correctly
        when(userRepositoryImpl.save(any(User.class))).thenReturn(updatedUser);

        // Act
        boolean result = userService.verifyTwoFactorCode(userId, code, true);

        // Assert
        assertTrue(result);
        verify(userRepositoryImpl).findById(userId);
        verify(userRepositoryImpl).save(any(User.class));
    }

    @Test
    void verifyTwoFactorCode_ShouldReturnFalse_WhenTwoFactorIsNotEnabled() {
        // Arrange
        String code = "123456";
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.of(testUser));
        when(testUser.isTwoFactorEnabled()).thenReturn(false);

        // Act
        boolean result = userService.verifyTwoFactorCode(userId, code, false);

        // Assert
        assertFalse(result);
        verify(userRepositoryImpl).findById(userId);
        verify(twoFactorAuthService, never()).verifyTotp(anyString(), anyString());
    }

    @Test
    void verifyTwoFactorCode_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String code = "123456";
        when(userRepositoryImpl.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.verifyTwoFactorCode(userId, code, false));
        verify(userRepositoryImpl).findById(userId);
        verify(twoFactorAuthService, never()).verifyTotp(anyString(), anyString());
    }

    @Test
    void recordFailedLoginAttempt_ShouldReturnTrue_WhenAccountIsLocked() {
        // Arrange
        String email = "test@example.com";
        User lockedUser = testUser.recordFailedLogin(5, 30); // Assuming this method exists and works correctly
        when(userRepositoryImpl.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userRepositoryImpl.save(any(User.class))).thenReturn(lockedUser);
        when(lockedUser.isAccountLocked()).thenReturn(true);

        // Act
        boolean result = userService.recordFailedLoginAttempt(email);

        // Assert
        assertTrue(result);
        verify(userRepositoryImpl).findByEmail(email);
        verify(userRepositoryImpl).save(any(User.class));
    }

    @Test
    void recordFailedLoginAttempt_ShouldReturnFalse_WhenUserDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepositoryImpl.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.recordFailedLoginAttempt(email);

        // Assert
        assertFalse(result);
        verify(userRepositoryImpl).findByEmail(email);
        verify(userRepositoryImpl, never()).save(any(User.class));
    }

    @Test
    void isAccountLocked_ShouldReturnTrue_WhenAccountIsLocked() {
        // Arrange
        String email = "test@example.com";
        when(userRepositoryImpl.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(testUser.isAccountLocked()).thenReturn(true);

        // Act
        boolean result = userService.isAccountLocked(email);

        // Assert
        assertTrue(result);
        verify(userRepositoryImpl).findByEmail(email);
    }

    @Test
    void isAccountLocked_ShouldReturnFalse_WhenAccountIsNotLocked() {
        // Arrange
        String email = "test@example.com";
        when(userRepositoryImpl.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(testUser.isAccountLocked()).thenReturn(false);

        // Act
        boolean result = userService.isAccountLocked(email);

        // Assert
        assertFalse(result);
        verify(userRepositoryImpl).findByEmail(email);
    }

    @Test
    void isAccountLocked_ShouldReturnFalse_WhenUserDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepositoryImpl.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        boolean result = userService.isAccountLocked(email);

        // Assert
        assertFalse(result);
        verify(userRepositoryImpl).findByEmail(email);
    }

    @Test
    void findAllUsers_ShouldReturnPageOfUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepositoryImpl.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<User> result = userService.findAllUsers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepositoryImpl).findAll(pageable);
    }
}
