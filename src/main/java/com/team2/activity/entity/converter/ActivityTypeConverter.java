package com.team2.activity.entity.converter;

import com.team2.activity.entity.enums.ActivityType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// ActivityType enum과 DB 문자열 컬럼 사이 변환을 담당한다.
@Converter(autoApply = true)
public class ActivityTypeConverter implements AttributeConverter<ActivityType, String> {

    // 엔티티 enum 값을 DB 저장 문자열로 변환한다.
    @Override
    public String convertToDatabaseColumn(ActivityType attribute) {
        // enum 값이 있으면 displayName을 꺼내고 없으면 null을 반환한다.
        return attribute != null ? attribute.getDisplayName() : null;
    }

    // DB 문자열 값을 엔티티 enum 값으로 복원한다.
    @Override
    public ActivityType convertToEntityAttribute(String dbData) {
        // DB 값이 있으면 enum으로 변환하고 없으면 null을 반환한다.
        return dbData != null ? ActivityType.from(dbData) : null;
    }
}
