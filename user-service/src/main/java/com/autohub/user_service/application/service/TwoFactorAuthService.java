package com.autohub.user_service.application.service;

import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling two-factor authentication operations.
 */
@Service
public class TwoFactorAuthService {

    private static final int SECRET_SIZE = 20;
    private static final int BACKUP_CODE_LENGTH = 8;
    private static final int NUMBER_OF_BACKUP_CODES = 10;
    private static final String BACKUP_CODE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    // TOTP codes are valid for 30 seconds by default in the aerogear implementation

    /**
     * Generates a random secret key for TOTP.
     *
     * @return Base32 encoded secret key
     */
    public String generateTotpSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_SIZE];
        random.nextBytes(bytes);
        return Base32.encode(bytes);
    }

    /**
     * Generates a list of backup codes for account recovery.
     *
     * @return List of backup codes
     */
    public List<String> generateBackupCodes() {
        List<String> backupCodes = new ArrayList<>(NUMBER_OF_BACKUP_CODES);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < NUMBER_OF_BACKUP_CODES; i++) {
            StringBuilder code = new StringBuilder(BACKUP_CODE_LENGTH);
            for (int j = 0; j < BACKUP_CODE_LENGTH; j++) {
                int index = random.nextInt(BACKUP_CODE_CHARS.length());
                code.append(BACKUP_CODE_CHARS.charAt(index));
            }
            backupCodes.add(code.toString());
        }

        return backupCodes;
    }

    /**
     * Verifies a TOTP code against a secret key.
     *
     * @param secret The TOTP secret key
     * @param code   The TOTP code to verify
     * @return true if the code is valid
     */
    public boolean verifyTotp(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }

        try {
            Totp totp = new Totp(secret);
            return totp.verify(code);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generates the current TOTP code for a secret key (for testing purposes).
     *
     * @param secret The TOTP secret key
     * @return The current TOTP code
     */
    public String generateCurrentTotp(String secret) {
        if (secret == null) {
            return null;
        }

        try {
            Totp totp = new Totp(secret);
            return totp.now();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Generates a URI for QR code generation.
     *
     * @param secret    The TOTP secret key
     * @param username  The username (typically email)
     * @param issuer    The issuer name (typically application name)
     * @return URI for QR code generation
     */
    public String generateQrCodeUri(String secret, String username, String issuer) {
        if (secret == null || username == null || issuer == null) {
            return null;
        }

        String encodedIssuer = encodeUriComponent(issuer);
        String encodedUsername = encodeUriComponent(username);

        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                encodedIssuer, encodedUsername, secret, encodedIssuer);
    }

    private String encodeUriComponent(String component) {
        // Simple URI encoding for common characters
        return component.replace(" ", "%20")
                .replace(":", "%3A")
                .replace("/", "%2F")
                .replace("?", "%3F")
                .replace("&", "%26")
                .replace("=", "%3D");
    }
}
