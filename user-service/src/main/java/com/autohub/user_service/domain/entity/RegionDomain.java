package com.autohub.user_service.domain.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model representing a Region in the business context,
 * independent of persistence concerns.
 */
@Getter
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode(of = {"id"})
public class RegionDomain {
    private final Integer id;
    private final String name;
    private final String code;

    /**
     * Factory method to create a simple region with just a name
     *
     * @param name The region name
     * @return A new RegionDomain instance with just a name
     */
    public static RegionDomain ofName(String name) {
        return RegionDomain.builder().name(name).build();
    }

    /**
     * Factory method to create a region with name and code
     *
     * @param name The region name
     * @param code The region code
     * @return A new RegionDomain instance with name and code
     */
    public static RegionDomain of(String name, String code) {
        return RegionDomain.builder()
                .name(name)
                .code(code)
                .build();
    }

    /**
     * Validates if the region has a valid code format
     *
     * @return true if code format is valid
     */
    public boolean hasValidCodeFormat() {
        return code != null && code.matches("[A-Z]{2,}");
    }

    /**
     * Checks if this region is complete with both name and code
     *
     * @return true if region has both name and code
     */
    public boolean isComplete() {
        return name != null && !name.isBlank() &&
                code != null && !code.isBlank();
    }

    /**
     * Returns the display name of the region, using the code as fallback
     *
     * @return The name to display for this region
     */
    public String getDisplayName() {
        if (name != null && !name.isBlank()) {
            return name;
        } else if (code != null && !code.isBlank()) {
            return code;
        } else {
            return "Unknown Region";
        }
    }

    /**
     * Returns the full representation of the region including code if available
     *
     * @return Formatted region name with code
     */
    public String getFullName() {
        if (name != null && code != null) {
            return String.format("%s (%s)", name, code);
        } else {
            return getDisplayName();
        }
    }

    /**
     * Creates a new region based on this one but with a different name
     *
     * @param newName The new name for the region
     * @return A new RegionDomain with updated name
     */
    public RegionDomain withName(String newName) {
        return toBuilder().name(newName).build();
    }

    /**
     * Creates a new region based on this one but with a different code
     *
     * @param newCode The new code for the region
     * @return A new RegionDomain with updated code
     */
    public RegionDomain withCode(String newCode) {
        return toBuilder().code(newCode).build();
    }

    /**
     * Compares region codes ignoring case
     *
     * @param otherCode Code to compare with
     * @return true if codes match (ignoring case)
     */
    public boolean matchesCode(String otherCode) {
        return code != null && code.equalsIgnoreCase(otherCode);
    }

    /**
     * Standardizes the region code to uppercase
     *
     * @return A new RegionDomain with standardized code
     */
    public RegionDomain withStandardizedCode() {
        if (code == null) {
            return this;
        }
        return toBuilder().code(code.toUpperCase()).build();
    }

    /**
     * Generates a code based on the name if code is missing
     * @return A new RegionDomain with generated code
     */
    public RegionDomain withGeneratedCodeIfMissing() {
        if (code != null && !code.isBlank()) {
            return this;
        }
        
        if (name == null || name.isBlank()) {
            return this;
        }
        
        // Generate a code from the first letters of each word in the name
        StringBuilder codeBuilder = new StringBuilder();
        String[] words = name.split("\\s+");
        
        for (String word : words) {
            if (!word.isEmpty()) {
                codeBuilder.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        
        String generatedCode = codeBuilder.toString();
        
        // Ensure code is at least 2 characters
        if (generatedCode.length() < 2 && name.length() >= 2) {
            generatedCode = name.substring(0, 2).toUpperCase();
        }
        
        return toBuilder().code(generatedCode).build();
    }
}
