package com.team2.activity.dto;

import jakarta.validation.constraints.NotBlank;

public record ContactCreateRequest(
        @NotBlank String contactName,
        String contactPosition,
        String contactEmail,
        String contactTel
) {}
