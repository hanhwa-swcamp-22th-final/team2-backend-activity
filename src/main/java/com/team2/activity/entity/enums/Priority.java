package com.team2.activity.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator; // JSON 역직렬화 시 사용할 팩토리 메서드 지정
import com.fasterxml.jackson.annotation.JsonValue;   // JSON 직렬화 시 반환할 값 지정

// 활동 우선순위 열거형 (DB: 'HIGH','MEDIUM') - 이슈 타입에서만 사용
public enum Priority implements DisplayNameEnum {
    HIGH("HIGH"),   // 높은 우선순위 - 긴급하게 처리해야 할 이슈 (구: 높음)
    MEDIUM("MEDIUM"); // 보통 우선순위 - 일반적인 이슈 (구: 보통)

    private final String displayName; // 프론트엔드 및 DB에서 사용하는 영문 표시값

    Priority(String displayName) {
        this.displayName = displayName; // 열거 상수 생성 시 영문 표시값 저장
    }

    @Override
    @JsonValue // Jackson이 직렬화 시 이 메서드의 반환값(영문)을 JSON에 사용
    public String getDisplayName() {
        return displayName; // 영문 표시값 반환 (예: "HIGH")
    }

    @JsonCreator // Jackson이 역직렬화 시 이 메서드로 문자열 → 열거 상수 변환
    public static Priority from(String value) {
        for (Priority p : values()) {          // 모든 열거 상수 순회
            if (p.displayName.equals(value)) { // 전달된 문자열과 영문 표시값 비교
                return p;                      // 일치하는 상수 반환
            }
        }
        throw new IllegalArgumentException("Unknown Priority: " + value); // 일치 없으면 예외 발생
    }
}
