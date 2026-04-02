package com.team2.activity.query.dto;

import com.team2.activity.command.domain.entity.Contact;

import java.time.LocalDateTime;

// 연락처 쓰기 API 응답에 사용하는 DTO record다.
public record ContactResponse(
        // 연락처 고유 식별자다.
        Long contactId,
        // 연관 거래처 ID다.
        Long clientId,
        // 연락처 작성자 ID다.
        Long writerId,
        // 연락처 이름이다.
        String contactName,
        // 연락처 직책이다.
        String contactPosition,
        // 연락처 이메일이다.
        String contactEmail,
        // 연락처 전화번호다.
        String contactTel,
        // 생성 시각이다.
        LocalDateTime createdAt,
        // 최종 수정 시각이다.
        LocalDateTime updatedAt
) {
    // Contact 엔티티에서 응답 DTO를 생성하는 정적 팩터리 메서드다.
    public static ContactResponse from(Contact contact) {
        // 엔티티 각 필드를 레코드 생성자에 순서대로 넘겨 DTO를 구성한다.
        return new ContactResponse(
                // 연락처 식별자를 복사한다.
                contact.getContactId(),
                // 거래처 ID를 복사한다.
                contact.getClientId(),
                // 작성자 ID를 복사한다.
                contact.getWriterId(),
                // 이름을 복사한다.
                contact.getContactName(),
                // 직책을 복사한다.
                contact.getContactPosition(),
                // 이메일을 복사한다.
                contact.getContactEmail(),
                // 전화번호를 복사한다.
                contact.getContactTel(),
                // 생성 시각을 복사한다.
                contact.getCreatedAt(),
                // 수정 시각을 복사한다.
                contact.getUpdatedAt()
        );
    }
}
