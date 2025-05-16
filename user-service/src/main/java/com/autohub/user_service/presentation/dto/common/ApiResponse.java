package com.autohub.user_service.presentation.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A standardized response format for all API endpoints.
 * This wrapper provides a consistent structure for success and error responses.
 *
 * @param <T> The type of data being returned
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private boolean success;
    private T data;
    private ErrorDetails error;
    
    /**
     * Creates a successful response with data
     *
     * @param data The data to include in the response
     * @param <T> The type of data
     * @return A new ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }
    
    /**
     * Creates a successful response without data
     *
     * @return A new ApiResponse instance
     */
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .success(true)
                .build();
    }
    
    /**
     * Creates an error response
     *
     * @param errorDetails The error details
     * @return A new ApiResponse instance
     */
    public static ApiResponse<Void> error(ErrorDetails errorDetails) {
        return ApiResponse.<Void>builder()
                .success(false)
                .error(errorDetails)
                .build();
    }
    
    /**
     * Creates an error response with a message and status
     *
     * @param message The error message
     * @param status The HTTP status code
     * @return A new ApiResponse instance
     */
    public static ApiResponse<Void> error(String message, int status) {
        return error(ErrorDetails.builder()
                .message(message)
                .status(status)
                .build());
    }
    
    /**
     * Creates an error response with a message, status, and code
     *
     * @param message The error message
     * @param status The HTTP status code
     * @param code The error code
     * @return A new ApiResponse instance
     */
    public static ApiResponse<Void> error(String message, int status, String code) {
        return error(ErrorDetails.builder()
                .message(message)
                .status(status)
                .code(code)
                .build());
    }
}
