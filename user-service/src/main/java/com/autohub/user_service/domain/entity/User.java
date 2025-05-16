package com.autohub.user_service.domain.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Domain model representing a User in the business context,
 * independent of persistence concerns.
 */
@Getter
@Builder(toBuilder = true)
@ToString(exclude = {"password", "verificationToken", "resetPasswordToken"})
@EqualsAndHashCode(of = {"id"})
public class User {
    private final UUID id;
    private final String email;
    private final String password;
    private final String phone;
    private final UserStatus status;
    private final String firstName;
    private final String secondName;
    private final String lastName;
    private final LocalDate birthDate;
    private final LocalDateTime lastLoginAt;
    private final boolean verified;
    private final String verificationToken;
    private final String resetPasswordToken;
    private final LocalDateTime resetPasswordExpires;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Set<Role> roles;
    private final String oauthProvider;
    private final String oauthProviderId;

    /**
     * Creates a new user with default values
     *
     * @param email    User's email
     * @param password User's password (should be already encrypted)
     * @return A new user domain object
     */
    public static User createNew(String email, String password) {
        return User.builder()
                .email(email)
                .password(password)
                .status(UserStatus.PENDING)
                .verified(false)
                .verificationToken(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .roles(new HashSet<>())
                .build();
    }

    /**
     * Creates a new user from OAuth2 authentication
     *
     * @param email           User's email
     * @param firstName       User's first name (if available)
     * @param lastName        User's last name (if available)
     * @param oauthProvider   OAuth2 provider name (e.g., "google", "facebook")
     * @param oauthProviderId User's ID with the OAuth2 provider
     * @return A new user domain object
     */
    public static User createFromOAuth2(String email, String firstName, String lastName, 
                                       String oauthProvider, String oauthProviderId) {
        return User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .status(UserStatus.ACTIVE) // OAuth2 users are automatically active
                .verified(true) // OAuth2 users are automatically verified
                .createdAt(LocalDateTime.now())
                .roles(new HashSet<>())
                .oauthProvider(oauthProvider)
                .oauthProviderId(oauthProviderId)
                .build();
    }

    /**
     * Gets the full name of the user
     *
     * @return Full name constructed from first, second and last name
     */
    public String getFullName() {
        StringBuilder builder = new StringBuilder();

        if (firstName != null && !firstName.isBlank()) {
            builder.append(firstName);
        }

        if (secondName != null && !secondName.isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(" ");
            }
            builder.append(secondName);
        }

        if (lastName != null && !lastName.isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(" ");
            }
            builder.append(lastName);
        }

        return !builder.isEmpty() ? builder.toString() : "Anonymous User";
    }

    /**
     * Checks if the user has the specified role
     *
     * @param roleType The role type to check
     * @return true if the user has the role
     */
    public boolean hasRole(RoleType roleType) {
        if (roles == null) {
            return false;
        }

        for (Role role : roles) {
            if (role.getRole() == roleType) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a role to the user
     *
     * @param roleType The role type to add
     * @return A new user domain with the role added
     */
    public User addRole(RoleType roleType) {
        if (hasRole(roleType)) {
            return this;
        }

        Set<Role> newRoles = new HashSet<>(roles != null ? roles : Collections.emptySet());
        newRoles.add(Role.create(id, roleType));

        return toBuilder()
                .roles(newRoles)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Removes a role from the user
     *
     * @param roleType The role type to remove
     * @return A new user domain with the role removed
     */
    public User removeRole(RoleType roleType) {
        if (!hasRole(roleType)) {
            return this;
        }

        Set<Role> newRoles = new HashSet<>(roles);
        newRoles.removeIf(role -> role.getRole() == roleType);

        return toBuilder()
                .roles(newRoles)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updates the user's login timestamp
     *
     * @return A new user domain with updated login time
     */
    public User recordLogin() {
        return toBuilder()
                .lastLoginAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Activates a user account
     *
     * @return A new user domain with active status
     */
    public User activate() {
        if (UserStatus.ACTIVE == status) {
            return this;
        }

        return toBuilder()
                .status(UserStatus.ACTIVE)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Deactivates a user account
     *
     * @return A new user domain with inactive status
     */
    public User deactivate() {
        if (UserStatus.INACTIVE == status) {
            return this;
        }

        return toBuilder()
                .status(UserStatus.INACTIVE)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Bans a user account
     *
     * @return A new user domain with banned status
     */
    public User ban() {
        if (UserStatus.BANNED == status) {
            return this;
        }

        return toBuilder()
                .status(UserStatus.BANNED)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Verifies a user account
     *
     * @param token The verification token to validate
     * @return A new user domain with verified status if token is valid
     */
    public User verify(String token) {
        if (verified || !Objects.equals(verificationToken, token)) {
            return this;
        }

        return toBuilder()
                .verified(true)
                .verificationToken(null)
                .status(UserStatus.ACTIVE)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Sets up password reset for the user
     *
     * @param expiryHours Number of hours until reset token expires
     * @return A new user domain with password reset token
     */
    public User initiatePasswordReset(int expiryHours) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(expiryHours);

        return toBuilder()
                .resetPasswordToken(token)
                .resetPasswordExpires(expiry)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Completes password reset process
     *
     * @param token       The reset token to validate
     * @param newPassword The new password (should already be encrypted)
     * @return A new user domain with updated password if token is valid
     */
    public User completePasswordReset(String token, String newPassword) {
        if (resetPasswordToken == null ||
                !resetPasswordToken.equals(token) ||
                resetPasswordExpires == null ||
                resetPasswordExpires.isBefore(LocalDateTime.now())) {
            return this;
        }

        return toBuilder()
                .password(newPassword)
                .resetPasswordToken(null)
                .resetPasswordExpires(null)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updates user profile information
     *
     * @param firstName  New first name (or null to keep existing)
     * @param secondName New second name (or null to keep existing)
     * @param lastName   New last name (or null to keep existing)
     * @param phone      New phone (or null to keep existing)
     * @param birthDate  New birth date (or null to keep existing)
     * @return A new user domain with updated profile information
     */
    public User updateProfile(
            String firstName,
            String secondName,
            String lastName,
            String phone,
            LocalDate birthDate) {

        return toBuilder()
                .firstName(firstName != null ? firstName : this.firstName)
                .secondName(secondName != null ? secondName : this.secondName)
                .lastName(lastName != null ? lastName : this.lastName)
                .phone(phone != null ? phone : this.phone)
                .birthDate(birthDate != null ? birthDate : this.birthDate)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Checks if the user account is active
     *
     * @return true if the account is active
     */
    public boolean isActive() {
        return UserStatus.ACTIVE == status;
    }

    /**
     * Checks if the user can log in
     *
     * @return true if the user can log in
     */
    public boolean canLogin() {
        return UserStatus.ACTIVE == status || UserStatus.PENDING == status;
    }

    /**
     * Checks if the password reset token is valid
     *
     * @param token The token to validate
     * @return true if the token is valid and not expired
     */
    public boolean isResetTokenValid(String token) {
        return resetPasswordToken != null &&
                resetPasswordToken.equals(token) &&
                resetPasswordExpires != null &&
                resetPasswordExpires.isAfter(LocalDateTime.now());
    }

    /**
     * Checks if the user's account is new (created recently)
     *
     * @param days Number of days to consider an account new
     * @return true if the account was created within the specified number of days
     */
    public boolean isNewAccount(int days) {
        if (createdAt == null) {
            return false;
        }

        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return createdAt.isAfter(threshold);
    }

    /**
     * Gets the age of the user
     *
     * @return The user's age in years, or empty if birth date is not set
     */
    public Optional<Integer> getAge() {
        if (birthDate == null) {
            return Optional.empty();
        }

        return Optional.of(LocalDate.now().getYear() - birthDate.getYear());
    }

    /**
     * Gets all roles as string representations
     *
     * @return List of role names
     */
    public List<String> getRoleNames() {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> roleNames = new ArrayList<>();
        for (Role role : roles) {
            roleNames.add(role.getRole().name());
        }
        return roleNames;
    }
}
