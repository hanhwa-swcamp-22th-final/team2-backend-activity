package com.team2.activity.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// 연락처 수정 요청 본문을 받는 DTO다.
@Schema(description = "연락처 수정 요청")
public record ContactUpdateRequest(
        @Schema(description = "연락처 이름", example = "홍길동")
        @NotBlank String contactName,
        @Schema(description = "직책", example = "부장")
        String contactPosition,
        @Schema(description = "이메일", example = "hong@example.com")
        String contactEmail,
        @Schema(description = "전화번호", example = "010-1234-5678")
        String contactTel
) {}
