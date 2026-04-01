package com.team2.activity.command.application.dto;

import jakarta.validation.constraints.NotBlank;

// 연락처 수정 요청 본문을 받는 DTO다.
public record ContactUpdateRequest(
        // 수정할 연락처 이름이다.
        @NotBlank String contactName,
        // 수정할 연락처 직책이다.
        String contactPosition,
        // 수정할 연락처 이메일이다.
        String contactEmail,
        // 수정할 연락처 전화번호다.
        String contactTel
) {}
