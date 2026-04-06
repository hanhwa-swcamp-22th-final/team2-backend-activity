package com.team2.activity.query.dto;

import com.team2.activity.command.domain.entity.EmailLogType;
import com.team2.activity.command.domain.entity.enums.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 첨부 문서 유형 응답")
public record EmailLogTypeResponse(
        @Schema(description = "이메일 문서 유형 ID") Long emailLogTypeId,
        @Schema(description = "문서 유형") DocumentType emailDocType
) {
    public static EmailLogTypeResponse from(EmailLogType emailLogType) {
        return new EmailLogTypeResponse(
                emailLogType.getEmailLogTypeId(),
                emailLogType.getEmailDocType()
        );
    }
}
