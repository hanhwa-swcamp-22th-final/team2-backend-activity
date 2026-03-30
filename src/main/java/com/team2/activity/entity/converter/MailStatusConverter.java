package com.team2.activity.entity.converter;

import com.team2.activity.entity.enums.MailStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MailStatusConverter implements AttributeConverter<MailStatus, String> {

    @Override
    public String convertToDatabaseColumn(MailStatus attribute) {
        return attribute != null ? attribute.getDisplayName() : null;
    }

    @Override
    public MailStatus convertToEntityAttribute(String dbData) {
        return dbData != null ? MailStatus.from(dbData) : null;
    }
}
