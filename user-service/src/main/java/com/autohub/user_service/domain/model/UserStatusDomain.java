package com.autohub.user_service.domain.model;

public enum UserStatusDomain {
    ACTIVE,
    INACTIVE,
    BANNED,
    PENDING;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean canLogin() {
        return this == ACTIVE || this == PENDING;
    }
}
