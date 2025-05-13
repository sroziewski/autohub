package com.autohub.user_service.domain.validation;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents a single validation error with details about the error.
 */
@Getter
@Builder
public class ValidationError {
    
    /**
     * The field that caused the validation error (if applicable)
     */
    private final String field;
    
    /**
     * Error code that can be used for programmatic handling
     */
    private final String code;
    
    /**
     * Human-readable error message
     */
    private final String message;
    
    /**
     * Additional context information about the error
     */
    private final Object context;
    
    /**
     * The severity of the validation error
     */
    @Builder.Default
    private final Severity severity = Severity.ERROR;
    
    /**
     * Enum representing validation error severity levels
     */
    public enum Severity {
        INFO, WARNING, ERROR
    }
}
