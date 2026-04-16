package com.team2.activity.command.domain.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator; // JSON 역직렬화 시 사용할 팩토리 메서드 지정
import com.fasterxml.jackson.annotation.JsonValue;   // JSON 직렬화 시 반환할 값 지정

// 활동 기록 유형 열거형 (DB: 'meeting','issue','memo','schedule')
public enum ActivityType implements DisplayNameEnum {
    MEETING("meeting"),   // 거래처와의 미팅 또는 협의 기록
    ISSUE("issue"),       // 문제 또는 이슈 기록, 우선순위 설정 가능
    MEMO("memo"),         // 자유 형식의 메모 또는 노트
    SCHEDULE("schedule"); // 일정 기록, 시작일/종료일 필수

    private final String displayName; // 프론트엔드 및 DB에서 사용하는 영문 표시값

    ActivityType(String displayName) {
        this.displayName = displayName; // 열거 상수 생성 시 영문 표시값 저장
    }

    @Override
    @JsonValue // Jackson이 직렬화 시 이 메서드의 반환값(영문)을 JSON에 사용
    public String getDisplayName() {
        return displayName; // 영문 표시값 반환 (예: "MEETING")
    }

    @JsonCreator // Jackson이 역직렬화 시 이 메서드로 문자열 → 열거 상수 변환
    public static ActivityType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Unknown ActivityType: null");
        }
        // displayName(소문자) / enum name(대문자) 모두 허용, 대소문자 무시
        for (ActivityType type : values()) {
            if (type.displayName.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ActivityType: " + value);
    }
}
