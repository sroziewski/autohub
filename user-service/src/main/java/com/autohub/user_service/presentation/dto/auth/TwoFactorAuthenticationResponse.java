package com.autohub.user_service.presentation.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for initial authentication when two-factor authentication is enabled.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorAuthenticationResponse {
    private String temporaryToken;
    private boolean twoFactorRequired;
    
    // Only set if twoFactorRequired is false
    private String token;
}
