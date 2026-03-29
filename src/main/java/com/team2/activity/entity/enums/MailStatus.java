package com.team2.activity.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator; // JSON 역직렬화 시 사용할 팩토리 메서드 지정
import com.fasterxml.jackson.annotation.JsonValue;   // JSON 직렬화 시 반환할 값 지정

// 메일 발송 상태 열거형 (DB ENUM: '발송','실패') - 기본값: SENT
public enum MailStatus {
    SENT("발송"),   // 메일 발송 성공 상태 (기본값)
    FAILED("실패"); // 메일 발송 실패 상태

    private final String displayName; // 프론트엔드 및 DB에서 사용하는 한글 표시값

    MailStatus(String displayName) {
        this.displayName = displayName; // 열거 상수 생성 시 한글 표시값 저장
    }

    @JsonValue // Jackson이 직렬화 시 이 메서드의 반환값(한글)을 JSON에 사용
    public String getDisplayName() {
        return displayName; // 한글 표시값 반환 (예: "발송")
    }

    @JsonCreator // Jackson이 역직렬화 시 이 메서드로 문자열 → 열거 상수 변환
    public static MailStatus from(String value) {
        for (MailStatus s : values()) {        // 모든 열거 상수 순회
            if (s.displayName.equals(value)) { // 전달된 문자열과 한글 표시값 비교
                return s;                      // 일치하는 상수 반환
            }
        }
        throw new IllegalArgumentException("Unknown MailStatus: " + value); // 일치 없으면 예외 발생
    }
}
