package com.team2.activity.query.dto;

import com.team2.activity.command.domain.entity.Contact;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// 연락처 쓰기 API 응답에 사용하는 DTO record다.
@Schema(description = "연락처 응답")
public record ContactResponse(
        @Schema(description = "연락처 ID") Long contactId,
        @Schema(description = "거래처 ID") Long clientId,
        @Schema(description = "작성자 ID") Long writerId,
        @Schema(description = "연락처 이름") String contactName,
        @Schema(description = "직책") String contactPosition,
        @Schema(description = "이메일") String contactEmail,
        @Schema(description = "전화번호") String contactTel,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "최종 수정 시각") LocalDateTime updatedAt
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
