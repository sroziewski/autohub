package com.autohub.user_service.domain.model;

public enum RoleTypeDomain {
    USER,
    SELLER,
    ADMIN,
    MODERATOR;

    /**
     * Checks if this role has administrative privileges
     * @return true if the role is an administrator role
     */
    public boolean isAdminRole() {
        return this == ADMIN;
    }

    /**
     * Checks if this role has moderation privileges
     * @return true if the role has moderation capabilities
     */
    public boolean hasModeratorPrivileges() {
        return this == ADMIN || this == MODERATOR;
    }

    /**
     * Checks if this role can sell products
     * @return true if the role can sell products
     */
    public boolean canSell() {
        return this == SELLER || this == ADMIN;
    }

    /**
     * Gets a user-friendly display name for the role
     * @return The display name for the role
     */
    public String getDisplayName() {
        return switch(this) {
            case USER -> "Basic User";
            case SELLER -> "Seller";
            case ADMIN -> "Administrator";
            case MODERATOR -> "Moderator";
        };
    }
}
