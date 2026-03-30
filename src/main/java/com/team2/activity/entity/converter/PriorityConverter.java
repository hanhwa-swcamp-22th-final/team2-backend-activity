package com.team2.activity.entity.converter;

import com.team2.activity.entity.enums.Priority;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PriorityConverter implements AttributeConverter<Priority, String> {

    @Override
    public String convertToDatabaseColumn(Priority attribute) {
        return attribute != null ? attribute.getDisplayName() : null;
    }

    @Override
    public Priority convertToEntityAttribute(String dbData) {
        return dbData != null ? Priority.from(dbData) : null;
    }
}
