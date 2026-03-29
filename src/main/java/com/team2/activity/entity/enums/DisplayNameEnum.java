package com.team2.activity.entity.enums;

// 한글 표시값을 가지는 열거형의 공통 계약 인터페이스
// RecordType, Priority, MailStatus, DocumentType 등 모든 표시값 열거형이 구현
public interface DisplayNameEnum {

    // JSON 직렬화 및 프론트엔드 표시에 사용되는 한글(또는 코드) 표시값 반환
    String getDisplayName();
}
