package com.autohub.user_service.presentation.controller.handler;

import com.autohub.user_service.domain.exception.InvalidEmailException;
import com.autohub.user_service.domain.exception.InvalidRoleException;
import com.autohub.user_service.domain.exception.ResourceNotFoundException;
import com.autohub.user_service.presentation.dto.common.ApiResponse;
import com.autohub.user_service.presentation.dto.common.ErrorDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for consistent error responses across all controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidEmail(InvalidEmailException ex) {
        log.error("Invalid email exception", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid email format", HttpStatus.BAD_REQUEST.value(), "INVALID_EMAIL"));
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRole(InvalidRoleException ex) {
        log.error("Invalid role exception", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid role", HttpStatus.BAD_REQUEST.value(), "INVALID_ROLE"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument exception", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "INVALID_ARGUMENT"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation exception", ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        ErrorDetails errorDetails = ErrorDetails.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .code("VALIDATION_ERROR")
                .fieldErrors(errors)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorDetails));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found exception", ex);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value(), "RESOURCE_NOT_FOUND"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.error("Bad credentials exception", ex);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid credentials", HttpStatus.UNAUTHORIZED.value(), "INVALID_CREDENTIALS"));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountLocked(LockedException ex) {
        log.error("Account locked exception", ex);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Account is locked", HttpStatus.FORBIDDEN.value(), "ACCOUNT_LOCKED"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch exception", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        "Invalid parameter type for " + ex.getName(),
                        HttpStatus.BAD_REQUEST.value(),
                        "TYPE_MISMATCH"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "An unexpected error occurred",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "INTERNAL_ERROR"));
    }
}
