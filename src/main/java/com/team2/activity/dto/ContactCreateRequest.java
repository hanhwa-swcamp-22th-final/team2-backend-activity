package com.team2.activity.dto;

import jakarta.validation.constraints.NotBlank;

// 연락처 생성 요청 본문을 받는 DTO다.
public record ContactCreateRequest(
        // 연락처 이름이다.
        @NotBlank String contactName,
        // 연락처 직책이다.
        String contactPosition,
        // 연락처 이메일이다.
        String contactEmail,
        // 연락처 전화번호다.
        String contactTel
) {}
