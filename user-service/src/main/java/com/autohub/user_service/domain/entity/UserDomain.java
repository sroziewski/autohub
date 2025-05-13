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
public class UserDomain {
    private final UUID id;
    private final String email;
    private final String password;
    private final String phone;
    private final UserStatusDomain status;
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
    private final Set<RoleDomain> roles;

    /**
     * Creates a new user with default values
     *
     * @param email    User's email
     * @param password User's password (should be already encrypted)
     * @return A new user domain object
     */
    public static UserDomain createNew(String email, String password) {
        return UserDomain.builder()
                .email(email)
                .password(password)
                .status(UserStatusDomain.PENDING)
                .verified(false)
                .verificationToken(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .roles(new HashSet<>())
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
    public boolean hasRole(RoleTypeDomain roleType) {
        if (roles == null) {
            return false;
        }

        for (RoleDomain role : roles) {
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
    public UserDomain addRole(RoleTypeDomain roleType) {
        if (hasRole(roleType)) {
            return this;
        }

        Set<RoleDomain> newRoles = new HashSet<>(roles != null ? roles : Collections.emptySet());
        newRoles.add(RoleDomain.create(id, roleType));

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
    public UserDomain removeRole(RoleTypeDomain roleType) {
        if (!hasRole(roleType)) {
            return this;
        }

        Set<RoleDomain> newRoles = new HashSet<>(roles);
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
    public UserDomain recordLogin() {
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
    public UserDomain activate() {
        if (UserStatusDomain.ACTIVE == status) {
            return this;
        }

        return toBuilder()
                .status(UserStatusDomain.ACTIVE)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Deactivates a user account
     *
     * @return A new user domain with inactive status
     */
    public UserDomain deactivate() {
        if (UserStatusDomain.INACTIVE == status) {
            return this;
        }

        return toBuilder()
                .status(UserStatusDomain.INACTIVE)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Bans a user account
     *
     * @return A new user domain with banned status
     */
    public UserDomain ban() {
        if (UserStatusDomain.BANNED == status) {
            return this;
        }

        return toBuilder()
                .status(UserStatusDomain.BANNED)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Verifies a user account
     *
     * @param token The verification token to validate
     * @return A new user domain with verified status if token is valid
     */
    public UserDomain verify(String token) {
        if (verified || !Objects.equals(verificationToken, token)) {
            return this;
        }

        return toBuilder()
                .verified(true)
                .verificationToken(null)
                .status(UserStatusDomain.ACTIVE)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Sets up password reset for the user
     *
     * @param expiryHours Number of hours until reset token expires
     * @return A new user domain with password reset token
     */
    public UserDomain initiatePasswordReset(int expiryHours) {
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
    public UserDomain completePasswordReset(String token, String newPassword) {
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
    public UserDomain updateProfile(
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
        return UserStatusDomain.ACTIVE == status;
    }

    /**
     * Checks if the user can log in
     *
     * @return true if the user can log in
     */
    public boolean canLogin() {
        return UserStatusDomain.ACTIVE == status || UserStatusDomain.PENDING == status;
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
        for (RoleDomain role : roles) {
            roleNames.add(role.getRole().name());
        }
        return roleNames;
    }
}
