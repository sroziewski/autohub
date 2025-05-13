package com.autohub.user_service.domain.validation;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the result of a validation operation, containing validation status
 * and any validation errors that occurred.
 */
@Getter
@Builder
public class ValidationResult {
    
    /**
     * List of validation errors, if any
     */
    @Builder.Default
    private final List<ValidationError> errors = new ArrayList<>();
    
    /**
     * Whether the validation was successful
     */
    private final boolean valid;
    
    /**
     * Creates a successful validation result with no errors
     *
     * @return A validation result indicating successful validation
     */
    public static ValidationResult valid() {
        return ValidationResult.builder().valid(true).build();
    }
    
    /**
     * Creates a failed validation result with the given error messages
     *
     * @param errorMessages List of error messages
     * @return A validation result indicating failed validation with error messages
     */
    public static ValidationResult invalid(List<String> errorMessages) {
        ValidationResult result = ValidationResult.builder().valid(false).build();
        
        if (errorMessages != null) {
            for (String message : errorMessages) {
                result.addError(message);
            }
        }
        
        return result;
    }
    
    /**
     * Creates a failed validation result with a single error message
     *
     * @param errorMessage Error message
     * @return A validation result indicating failed validation with an error message
     */
    public static ValidationResult invalid(String errorMessage) {
        return invalid(Collections.singletonList(errorMessage));
    }
    
    /**
     * Creates a failed validation result with the given error
     *
     * @param error Validation error
     * @return A validation result indicating failed validation with an error
     */
    public static ValidationResult invalid(ValidationError error) {
        ValidationResult result = ValidationResult.builder().valid(false).build();
        result.addError(error);
        return result;
    }
    
    /**
     * Adds an error to the validation result
     *
     * @param error Validation error to add
     */
    public void addError(ValidationError error) {
        errors.add(error);
    }
    
    /**
     * Adds an error with the given message to the validation result
     *
     * @param message Error message
     */
    public void addError(String message) {
        errors.add(ValidationError.builder().message(message).build());
    }
    
    /**
     * Gets a list of all error messages
     *
     * @return List of error messages
     */
    public List<String> getErrorMessages() {
        return errors.stream()
                .map(ValidationError::getMessage)
                .toList();
    }
    
    /**
     * Combines this validation result with another validation result
     *
     * @param other Another validation result
     * @return Combined validation result
     */
    public ValidationResult combine(ValidationResult other) {
        if (other == null || other.isValid()) {
            return this;
        }
        
        ValidationResult result = ValidationResult.builder()
                .valid(this.isValid() && other.isValid())
                .build();
        
        result.errors.addAll(this.errors);
        result.errors.addAll(other.errors);
        
        return result;
    }
    
    /**
     * Checks if the validation result has any errors
     *
     * @return true if there are no errors, false otherwise
     */
    public boolean isValid() {
        return valid && errors.isEmpty();
    }
}
