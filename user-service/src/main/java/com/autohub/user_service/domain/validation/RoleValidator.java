package com.autohub.user_service.domain.validation;

import com.autohub.user_service.domain.entity.RoleDomain;
import com.autohub.user_service.domain.entity.RoleTypeDomain;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validator for RoleDomain objects ensuring various business rules are met.
 */
@Component
public class RoleValidator {

    // Maximum number of admin roles per system
    private static final int MAX_ADMIN_ROLES = 3;

    private final MessageSource messageSource;

    public RoleValidator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Gets a localized message from the message source
     *
     * @param key  Message key
     * @param args Message arguments
     * @return Localized message
     */
    private String getMessage(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, locale);
    }

    /**
     * Validates a role domain object
     *
     * @param roleDomain RoleDomain object to validate
     * @return Validation result with errors if invalid
     */
    public ValidationResult validate(RoleDomain roleDomain) {
        if (roleDomain == null) {
            ValidationError error = ValidationError.builder()
                    .code("ROLE_NULL")
                    .message("validation.role.null")
                    .build();
            return ValidationResult.invalid(error);
        }

        List<ValidationError> errors = new ArrayList<>();

        if (roleDomain.getUserId() == null) {
            errors.add(ValidationError.builder()
                    .field("userId")
                    .code("USER_ID_REQUIRED")
                    .message("validation.role.userId.required")
                    .build());
        }

        if (roleDomain.getRole() == null) {
            errors.add(ValidationError.builder()
                    .field("role")
                    .code("ROLE_TYPE_REQUIRED")
                    .message("validation.role.type.required")
                    .build());
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        } else {
            ValidationResult result = ValidationResult.builder().valid(false).build();
            errors.forEach(result::addError);
            return result;
        }
    }

    /**
     * Validates a role assignment request
     *
     * @param userId   User ID to assign the role to
     * @param roleType Role type to assign
     * @return Validation result with errors if invalid
     */
    private ValidationResult validateRoleAssignment(UUID userId, RoleTypeDomain roleType) {
        List<ValidationError> errors = new ArrayList<>();

        if (userId == null) {
            errors.add(ValidationError.builder()
                    .field("userId")
                    .code("USER_ID_REQUIRED")
                    .message("validation.role.userId.required")
                    .build());
        }

        if (roleType == null) {
            errors.add(ValidationError.builder()
                    .field("roleType")
                    .code("ROLE_TYPE_REQUIRED")
                    .message("validation.role.type.required")
                    .build());
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        } else {
            ValidationResult result = ValidationResult.builder().valid(false).build();
            errors.forEach(result::addError);
            return result;
        }
    }

    /**
     * Validates a role promotion request based on current roles and system state
     *
     * @param currentRole    The user's current role
     * @param targetRole     The role being promoted to
     * @param adminRoleCount Current count of admin roles in the system
     * @return Validation result with errors if promotion is invalid
     */
    public ValidationResult validateRolePromotion(RoleDomain currentRole, RoleTypeDomain targetRole, int adminRoleCount) {
        List<ValidationError> errors = new ArrayList<>();

        if (currentRole == null) {
            errors.add(ValidationError.builder()
                    .field("currentRole")
                    .code("CURRENT_ROLE_REQUIRED")
                    .message("validation.role.current.required")
                    .build());

            ValidationResult result = ValidationResult.builder().valid(false).build();
            errors.forEach(result::addError);
            return result;
        }

        if (targetRole == null) {
            errors.add(ValidationError.builder()
                    .field("targetRole")
                    .code("TARGET_ROLE_REQUIRED")
                    .message("validation.role.target.required")
                    .build());

            ValidationResult result = ValidationResult.builder().valid(false).build();
            errors.forEach(result::addError);
            return result;
        }

        // If promoting to the same role, it's redundant
        if (currentRole.getRole() == targetRole) {
            errors.add(ValidationError.builder()
                    .field("targetRole")
                    .code("SAME_ROLE_PROMOTION")
                    .message("validation.role.promotion.same")
                    .severity(ValidationError.Severity.WARNING)
                    .build());
        }

        // If the target role is admin, check system limit
        if (targetRole.isAdminRole() && adminRoleCount >= MAX_ADMIN_ROLES) {
            errors.add(ValidationError.builder()
                    .field("targetRole")
                    .code("ADMIN_ROLE_LIMIT")
                    .message("validation.role.admin.limit")
                    .context(Map.of("limit", MAX_ADMIN_ROLES, "current", adminRoleCount))
                    .build());
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        } else {
            ValidationResult result = ValidationResult.builder().valid(false).build();
            errors.forEach(result::addError);
            return result;
        }
    }

    /**
     * Validates if a role can be removed from a user
     *
     * @param roleDomain     The role to be removed
     * @param userRolesCount Total number of roles the user has
     * @return Validation result with errors if removal is invalid
     */
    private ValidationResult validateRoleRemoval(RoleDomain roleDomain, int userRolesCount) {
        List<ValidationError> errors = new ArrayList<>();

        if (roleDomain == null) {
            errors.add(ValidationError.builder()
                    .code("ROLE_NULL")
                    .message("validation.role.null")
                    .build());

            ValidationResult result = ValidationResult.builder().valid(false).build();
            errors.forEach(result::addError);
            return result;
        }

        // Ensure user doesn't lose all roles
        if (userRolesCount <= 1) {
            errors.add(ValidationError.builder()
                    .code("MINIMUM_ROLE_REQUIREMENT")
                    .message("validation.role.removal.minimum")
                    .context(userRolesCount)
                    .build());
        }

        // Check for special requirements for admin role removal
        if (roleDomain.isAdminRole()) {
            // Example: Special validation for admin role removal
            if (roleDomain.isAssignedWithin(90)) { // 90 days cooling period for admin role
                errors.add(ValidationError.builder()
                        .field("role")
                        .code("ADMIN_REMOVAL_COOLING_PERIOD")
                        .message("validation.role.admin.removal.cooling")
                        .context(Map.of("coolingPeriod", 90, "assignedAt", roleDomain.getAssignedAt()))
                        .build());
            }
        }

        if (errors.isEmpty()) {
            return ValidationResult.valid();
        } else {
            ValidationResult result = ValidationResult.builder().valid(false).build();
            errors.forEach(result::addError);
            return result;
        }
    }
}
