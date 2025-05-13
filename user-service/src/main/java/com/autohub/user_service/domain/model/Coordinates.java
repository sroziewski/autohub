package com.autohub.user_service.domain.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates implements Serializable {
    private Double longitude;
    private Double latitude;
    
    @Override
    public String toString() {
        return String.format("(%f,%f)", longitude, latitude);
    }
    
    public static Coordinates fromString(String pointStr) {
        if (pointStr == null || pointStr.isEmpty()) {
            return null;
        }
        
        String cleaned = pointStr.replace("(", "").replace(")", "");
        String[] parts = cleaned.split(",");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid point format: " + pointStr);
        }
        
        try {
            double x = Double.parseDouble(parts[0].trim());
            double y = Double.parseDouble(parts[1].trim());
            return new Coordinates(x, y);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid coordinate values in: " + pointStr, e);
        }
    }
}
