package com.team2.activity.command.domain.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator; // JSON 역직렬화 시 사용할 팩토리 메서드 지정
import com.fasterxml.jackson.annotation.JsonValue;   // JSON 직렬화 시 반환할 값 지정

// 메일 발송 상태 열거형 (DB: 'pending','sending','sent','failed') - 기본값: pending
public enum MailStatus implements DisplayNameEnum {
    PENDING("pending"),   // 발송 시도 전 초기 대기 상태
    SENDING("sending"),   // 재전송 중간 상태 — 중복 클릭/경쟁 방지용 마커
    SENT("sent"),         // 메일 발송 성공 상태
    FAILED("failed");     // 메일 발송 실패 상태

    private final String displayName; // 프론트엔드 및 DB에서 사용하는 영문 표시값

    MailStatus(String displayName) {
        this.displayName = displayName; // 열거 상수 생성 시 영문 표시값 저장
    }

    @Override
    @JsonValue // Jackson이 직렬화 시 이 메서드의 반환값(영문)을 JSON에 사용
    public String getDisplayName() {
        return displayName; // 영문 표시값 반환 (예: "SENT")
    }

    @JsonCreator // Jackson이 역직렬화 시 이 메서드로 문자열 → 열거 상수 변환
    public static MailStatus from(String value) {
        if (value == null) {                                // null 방어 (NPE 방지)
            throw new IllegalArgumentException("Unknown MailStatus: null");
        }
        for (MailStatus s : values()) {                     // 모든 열거 상수 순회
            if (s.displayName.equalsIgnoreCase(value)       // 소문자 표시값 ("sent"/"pending"/"failed")
                    || s.name().equalsIgnoreCase(value)) {  // 대문자 enum 이름 ("SENT"/"PENDING"/"FAILED") 둘 다 허용
                return s;                                   // 일치하는 상수 반환
            }
        }
        throw new IllegalArgumentException("Unknown MailStatus: " + value); // 일치 없으면 예외 발생
    }
}
