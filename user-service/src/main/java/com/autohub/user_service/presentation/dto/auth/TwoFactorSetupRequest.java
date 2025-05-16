package com.autohub.user_service.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for setting up two-factor authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorSetupRequest {
    // The verification code from the authenticator app to confirm setup
    @NotBlank(message = "Verification code is required")
    private String code;
    
    // The secret key generated during setup initiation
    @NotBlank(message = "Secret key is required")
    private String secret;
}
