package com.team2.activity.dto;

import jakarta.validation.constraints.NotBlank;

public record ContactUpdateRequest(
        @NotBlank String contactName,
        String contactPosition,
        String contactEmail,
        String contactTel
) {}
