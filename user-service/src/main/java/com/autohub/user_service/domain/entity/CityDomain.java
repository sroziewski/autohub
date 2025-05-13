package com.autohub.user_service.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Domain model representing a City in the business context,
 * independent of persistence concerns.
 */
@Getter
@Builder(toBuilder = true)  // Add toBuilder = true to enable toBuilder() method
@ToString
public class CityDomain {
    private final Integer id;
    private final Integer regionId;
    private final String name;
    private final String postalCodePattern;
    private final String regionName;
    private final String regionCode;
    
    /**
     * Validates if a postal code matches this city's pattern
     * @param postalCode The postal code to validate
     * @return true if the postal code matches the city's pattern or if pattern is not defined
     */
    public boolean isValidPostalCode(String postalCode) {
        if (postalCode == null || postalCode.isEmpty()) {
            return false;
        }
        
        if (postalCodePattern == null || postalCodePattern.isEmpty()) {
            return true; // No specific pattern to validate against
        }
        
        try {
            return Pattern.compile(postalCodePattern).matcher(postalCode).matches();
        } catch (Exception e) {
            // If the pattern is invalid, be lenient
            return true;
        }
    }
    
    /**
     * Generates an example postal code based on the pattern
     * @return An example postal code or empty if no pattern is defined
     */
    public Optional<String> generateExamplePostalCode() {
        if (postalCodePattern == null || postalCodePattern.isEmpty()) {
            return Optional.empty();
        }
        
        // Very simple example generator - replace specific parts of a pattern
        String example = postalCodePattern
            .replaceAll("\\\\d", "0")      // Replace a digit pattern with 0
            .replaceAll("\\[\\d-\\d\\]", "5") // Replace ranges with a middle value
            .replaceAll("\\\\w", "X")      // Replace word chars with X
            .replaceAll("\\*", "")         // Remove quantifiers
            .replaceAll("\\+", "")
            .replaceAll("\\?", "")
            .replaceAll("\\{\\d+(,\\d+)?\\}", "");
        
        return example.isEmpty() ? Optional.empty() : Optional.of(example);
    }
    
    /**
     * Determines if this city is in the specified region
     * @param regionIdToCheck The region ID to check against
     * @return true if the city is in the specified region
     */
    public boolean isInRegion(Integer regionIdToCheck) {
        return regionId != null && regionId.equals(regionIdToCheck);
    }
    
    /**
     * Determines if this city is in the region identified by the given code
     * @param regionCodeToCheck The region code to check against
     * @return true if the city is in the region with the specified code
     */
    public boolean isInRegionWithCode(String regionCodeToCheck) {
        return regionCode != null && regionCode.equals(regionCodeToCheck);
    }
    
    /**
     * Formats the city's name with region for display
     * @return Formatted city name with region
     */
    public String getFormattedName() {
        return name + (regionName != null ? ", " + regionName : "");
    }
    
    /**
     * Returns a qualified city name in standard format
     * @return City name with region code in parentheses if available
     */
    public String getQualifiedName() {
        return name + (regionCode != null ? " (" + regionCode + ")" : "");
    }
    
    /**
     * Creates a new CityDomain instance based on this one but with updated name
     * @param newName The new name for the city
     * @return New CityDomain instance with updated name
     */
    public CityDomain withName(String newName) {
        return toBuilder().name(newName).build();
    }
    
    /**
     * Creates a new CityDomain instance based on this one but with updated postal code pattern
     * @param newPattern The new postal code pattern
     * @return New CityDomain instance with updated postal code pattern
     */
    public CityDomain withPostalCodePattern(String newPattern) {
        return toBuilder().postalCodePattern(newPattern).build();
    }
    
    /**
     * Makes this city part of a new region
     * @param newRegionId ID of the new region
     * @param newRegionName Name of the new region
     * @param newRegionCode Code of the new region
     * @return New CityDomain instance with updated region information
     */
    public CityDomain moveToRegion(Integer newRegionId, String newRegionName, String newRegionCode) {
        return toBuilder()
            .regionId(newRegionId)
            .regionName(newRegionName)
            .regionCode(newRegionCode)
            .build();
    }
}
