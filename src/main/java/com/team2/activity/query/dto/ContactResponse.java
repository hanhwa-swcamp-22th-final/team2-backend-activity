package com.team2.activity.query.dto;

import com.team2.activity.command.domain.entity.Contact;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// 연락처 쓰기 API 응답에 사용하는 DTO record다.
@Schema(description = "연락처 응답 — 영업담당자 개인 주소록 (거래처 무관)")
public record ContactResponse(
        @Schema(description = "연락처 ID") Long contactId,
        @Schema(description = "작성자 ID") Long writerId,
        @Schema(description = "연락처 이름") String contactName,
        @Schema(description = "직책") String contactPosition,
        @Schema(description = "이메일") String contactEmail,
        @Schema(description = "전화번호") String contactTel,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "최종 수정 시각") LocalDateTime updatedAt
) {
    public static ContactResponse from(Contact contact) {
        return new ContactResponse(
                contact.getContactId(),
                contact.getWriterId(),
                contact.getContactName(),
                contact.getContactPosition(),
                contact.getContactEmail(),
                contact.getContactTel(),
                contact.getCreatedAt(),
                contact.getUpdatedAt()
        );
    }
}
