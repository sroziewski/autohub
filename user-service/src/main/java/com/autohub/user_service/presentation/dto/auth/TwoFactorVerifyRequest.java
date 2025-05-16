package com.autohub.user_service.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for two-factor authentication verification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorVerifyRequest {
    @NotBlank(message = "Verification code is required")
    private String code;
    
    // Token received after successful password authentication
    @NotBlank(message = "Temporary token is required")
    private String temporaryToken;
    
    // Flag to indicate if this is a backup code instead of a TOTP code
    private boolean isBackupCode;
}
