package com.team2.activity.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator; // JSON 역직렬화 시 사용할 팩토리 메서드 지정
import com.fasterxml.jackson.annotation.JsonValue;   // JSON 직렬화 시 반환할 값 지정

// 메일 첨부 문서 유형 열거형 (DB: 'PI','CI','PL','PRODUCTION_ORDER','SHIPPING_ORDER')
public enum DocumentType implements DisplayNameEnum {
    PI("PI"),                                    // 선적전검사의뢰서 (Pre-shipment Inspection)
    CI("CI"),                                    // 상업송장 (Commercial Invoice)
    PL("PL"),                                    // 포장명세서 (Packing List)
    PRODUCTION_ORDER("PRODUCTION_ORDER"),        // 공장에 생산을 지시하는 내부 문서 (구: 생산지시서)
    SHIPPING_ORDER("SHIPPING_ORDER");            // 물품 출하를 지시하는 내부 문서 (구: 출하지시서)

    private final String displayName; // 프론트엔드 및 DB에서 사용하는 영문 표시값

    DocumentType(String displayName) {
        this.displayName = displayName; // 열거 상수 생성 시 영문 표시값 저장
    }

    @Override
    @JsonValue // Jackson이 직렬화 시 이 메서드의 반환값(영문)을 JSON에 사용
    public String getDisplayName() {
        return displayName; // 영문 표시값 반환 (예: "PI", "PRODUCTION_ORDER")
    }

    @JsonCreator // Jackson이 역직렬화 시 이 메서드로 문자열 → 열거 상수 변환
    public static DocumentType from(String value) {
        for (DocumentType type : values()) {      // 모든 열거 상수 순회
            if (type.displayName.equals(value)) { // 전달된 문자열과 영문 표시값 비교
                return type;                      // 일치하는 상수 반환
            }
        }
        throw new IllegalArgumentException("Unknown DocumentType: " + value); // 일치 없으면 예외 발생
    }
}
