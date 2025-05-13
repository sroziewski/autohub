package com.autohub.user_service.infrastructure.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.data.geo.Point;

@Converter
public class PointConverter implements AttributeConverter<Point, String> {

    @Override
    public String convertToDatabaseColumn(Point point) {
        return point == null ? null : String.format("(%f,%f)", point.getX(), point.getY());
    }

    @Override
    public Point convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        
        String cleaned = dbData.replace("(", "").replace(")", "");
        String[] parts = cleaned.split(",");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid point format: " + dbData);
        }
        
        try {
            double x = Double.parseDouble(parts[0].trim());
            double y = Double.parseDouble(parts[1].trim());
            return new Point(x, y);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid coordinate values in: " + dbData, e);
        }
    }
}
