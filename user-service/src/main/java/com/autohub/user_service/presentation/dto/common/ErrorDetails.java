package com.autohub.user_service.presentation.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Contains details about an error that occurred during API processing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * Human-readable error message
     */
    private String message;
    
    /**
     * Application-specific error code
     */
    private String code;
    
    /**
     * Additional details about the error
     */
    private Map<String, Object> details;
    
    /**
     * Field-specific validation errors
     */
    private Map<String, String> fieldErrors;
}
