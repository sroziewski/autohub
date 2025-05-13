package com.autohub.user_service.domain.converter;

import com.autohub.user_service.domain.model.Coordinates;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PointConverter implements AttributeConverter<Coordinates, String> {

    @Override
    public String convertToDatabaseColumn(Coordinates coordinates) {
        return coordinates == null ? null : coordinates.toString();
    }

    @Override
    public Coordinates convertToEntityAttribute(String dbData) {
        return Coordinates.fromString(dbData);
    }
}
