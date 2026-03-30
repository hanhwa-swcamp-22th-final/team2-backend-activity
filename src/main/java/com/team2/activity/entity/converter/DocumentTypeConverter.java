package com.team2.activity.entity.converter;

import com.team2.activity.entity.enums.DocumentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DocumentTypeConverter implements AttributeConverter<DocumentType, String> {

    @Override
    public String convertToDatabaseColumn(DocumentType attribute) {
        return attribute != null ? attribute.getDisplayName() : null;
    }

    @Override
    public DocumentType convertToEntityAttribute(String dbData) {
        return dbData != null ? DocumentType.from(dbData) : null;
    }
}
