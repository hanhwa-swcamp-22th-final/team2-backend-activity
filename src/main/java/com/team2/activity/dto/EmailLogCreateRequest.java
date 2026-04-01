package com.team2.activity.dto;

import com.team2.activity.entity.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EmailLogCreateRequest(
        @NotNull Long clientId,
        String poId,
        @NotBlank String emailTitle,
        String emailRecipientName,
        @NotBlank String emailRecipientEmail,
        List<DocumentType> docTypes
) {}
