package com.autohub.user_service.domain.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model representing a user role in the business context,
 * independent of persistence concerns.
 */
@Getter
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode(of = {"userId", "role"})
public class Role {
    private final Long id;
    private final UUID userId;
    private final RoleType role;
    private final LocalDateTime assignedAt;

    /**
     * Factory method to create a new role for a user
     *
     * @param userId   The ID of the user
     * @param roleType The type of role to assign
     * @return A new RoleDomain instance
     */
    public static Role create(UUID userId, RoleType roleType) {
        return Role.builder()
                .userId(userId)
                .role(roleType)
                .assignedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Checks if this role has administrative privileges
     *
     * @return true if the role is an admin role
     */
    public boolean isAdminRole() {
        return role.isAdminRole();
    }

    /**
     * Checks if this role has moderation capabilities
     *
     * @return true if the role can moderate
     */
    public boolean hasModeratorPrivileges() {
        return role.hasModeratorPrivileges();
    }

    /**
     * Checks if this role can sell products
     *
     * @return true if the role can sell products
     */
    public boolean canSell() {
        return role.canSell();
    }

    /**
     * Checks if the role was assigned within the specified time period
     *
     * @param days Number of days to check
     * @return true if the role was assigned within the specified number of days
     */
    public boolean isAssignedWithin(int days) {
        if (assignedAt == null) {
            return false;
        }

        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return assignedAt.isAfter(threshold);
    }

    /**
     * Checks if this role matches the specified role type
     *
     * @param roleType Role type to check against
     * @return true if this role matches the specified role type
     */
    public boolean is(RoleType roleType) {
        return role == roleType;
    }

    /**
     * Returns a new role domain with a different role type
     *
     * @param newRoleType The new role type
     * @return A new RoleDomain instance with updated role type
     */
    public Role withRoleType(RoleType newRoleType) {
        if (role == newRoleType) {
            return this;
        }

        return toBuilder()
                .role(newRoleType)
                .assignedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Gets the duration for which this role has been assigned
     *
     * @return A string representation of how long the role has been assigned
     */
    public String getAssignmentDuration() {
        if (assignedAt == null) {
            return "Unknown";
        }

        LocalDateTime now = LocalDateTime.now();
        long days = java.time.Duration.between(assignedAt, now).toDays();

        if (days < 1) {
            return "Today";
        } else if (days == 1) {
            return "Yesterday";
        } else if (days < 30) {
            return days + " days ago";
        } else if (days < 365) {
            long months = days / 30;
            return months + (months == 1 ? " month ago" : " months ago");
        } else {
            long years = days / 365;
            return years + (years == 1 ? " year ago" : " years ago");
        }
    }

}
