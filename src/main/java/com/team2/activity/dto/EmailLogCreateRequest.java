package com.team2.activity.dto;

import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.EmailLogType;
import com.team2.activity.entity.enums.DocumentType;
import com.team2.activity.entity.enums.MailStatus;
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
) {
    public EmailLog toEntity(Long userId) {
        List<EmailLogType> docTypeList = docTypes != null
                ? docTypes.stream().map(EmailLogType::of).toList()
                : List.of();
        return EmailLog.builder()
                .clientId(clientId)
                .poId(poId)
                .emailTitle(emailTitle)
                .emailRecipientName(emailRecipientName)
                .emailRecipientEmail(emailRecipientEmail)
                .emailSenderId(userId)
                .emailStatus(MailStatus.SENT)
                .docTypes(docTypeList)
                .build();
    }
}
