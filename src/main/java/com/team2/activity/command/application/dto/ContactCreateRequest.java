package com.team2.activity.command.application.dto;

import com.team2.activity.command.domain.entity.Contact;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// 연락처 생성 요청 본문을 받는 DTO다.
@Schema(description = "연락처 생성 요청")
public record ContactCreateRequest(
        @Schema(description = "거래처 ID (선택 — 자유 컨택은 null)") Long clientId,
        @Schema(description = "연락처 이름", example = "홍길동")
        @NotBlank String contactName,
        @Schema(description = "직책", example = "과장")
        String contactPosition,
        @Schema(description = "이메일", example = "hong@example.com")
        String contactEmail,
        @Schema(description = "전화번호", example = "010-1234-5678")
        String contactTel
) {
    /** path 변수 clientId 우선, 없으면 body 의 clientId. 둘 다 null 이면 자유 컨택. */
    public Contact toEntity(Long clientIdOverride, Long writerId) {
        return Contact.builder()
                .clientId(clientIdOverride != null ? clientIdOverride : clientId)
                .writerId(writerId)
                .contactName(contactName)
                .contactPosition(contactPosition)
                .contactEmail(contactEmail)
                .contactTel(contactTel)
                .build();
    }
}
