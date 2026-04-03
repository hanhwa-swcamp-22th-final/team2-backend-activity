package com.team2.activity.query.dto;

import com.team2.activity.command.domain.entity.EmailLogType;
import com.team2.activity.command.domain.entity.enums.DocumentType;

public record EmailLogTypeResponse(
        Long emailLogTypeId,
        DocumentType emailDocType
) {
    public static EmailLogTypeResponse from(EmailLogType emailLogType) {
        return new EmailLogTypeResponse(
                emailLogType.getEmailLogTypeId(),
                emailLogType.getEmailDocType()
        );
    }
}
