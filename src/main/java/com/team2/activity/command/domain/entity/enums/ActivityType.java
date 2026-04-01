package com.team2.activity.command.domain.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator; // JSON 역직렬화 시 사용할 팩토리 메서드 지정
import com.fasterxml.jackson.annotation.JsonValue;   // JSON 직렬화 시 반환할 값 지정

// 활동 기록 유형 열거형 (DB: 'MEETING','ISSUE','MEMO','SCHEDULE')
public enum ActivityType implements DisplayNameEnum {
    MEETING("MEETING"),  // 거래처와의 미팅 또는 협의 기록 (구: 미팅/협의)
    ISSUE("ISSUE"),      // 문제 또는 이슈 기록, 우선순위 설정 가능 (구: 이슈)
    MEMO("MEMO"),        // 자유 형식의 메모 또는 노트 (구: 메모/노트)
    SCHEDULE("SCHEDULE"); // 일정 기록, 시작일/종료일 필수 (구: 일정)

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
        for (ActivityType type : values()) {        // 모든 열거 상수 순회
            if (type.displayName.equals(value)) {   // 전달된 문자열과 영문 표시값 비교
                return type;                        // 일치하는 상수 반환
            }
        }
        throw new IllegalArgumentException("Unknown ActivityType: " + value); // 일치 없으면 예외 발생
    }
}
