package com.team2.activity.entity.converter;

import com.team2.activity.entity.enums.ActivityType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActivityTypeConverter implements AttributeConverter<ActivityType, String> {

    @Override
    public String convertToDatabaseColumn(ActivityType attribute) {
        return attribute != null ? attribute.getDisplayName() : null;
    }

    @Override
    public ActivityType convertToEntityAttribute(String dbData) {
        return dbData != null ? ActivityType.from(dbData) : null;
    }
}
