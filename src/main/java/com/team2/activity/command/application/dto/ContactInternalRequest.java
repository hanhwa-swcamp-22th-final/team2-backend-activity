package com.team2.activity.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 내부 호출 전용 연락처 생성 요청 (Master의 Buyer 저장 시 Feign으로 전달).
@Schema(description = "내부 연락처 생성 요청 (Master → Activity)")
public record ContactInternalRequest(
        @NotNull @Schema(description = "거래처 ID") Long clientId,
        @NotNull @Schema(description = "작성자 ID") Long writerId,
        @NotBlank @Schema(description = "연락처 이름") String contactName,
        @Schema(description = "직책") String contactPosition,
        @Schema(description = "이메일") String contactEmail,
        @Schema(description = "전화번호") String contactTel
) {
}
