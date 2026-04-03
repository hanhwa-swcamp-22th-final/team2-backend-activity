package com.team2.activity.query.dto;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;

import java.time.LocalDateTime;
import java.util.List;

public record EmailLogResponse(
        Long emailLogId,
        Long clientId,
        String clientName,
        String poId,
        String emailTitle,
        String emailRecipientName,
        String emailRecipientEmail,
        Long emailSenderId,
        MailStatus emailStatus,
        LocalDateTime emailSentAt,
        LocalDateTime createdAt,
        List<EmailLogTypeResponse> docTypes,
        String senderName
) {
    public static EmailLogResponse from(EmailLog emailLog) {
        return from(emailLog, null, null);
    }

    public static EmailLogResponse from(EmailLog emailLog, String senderName) {
        return from(emailLog, senderName, null);
    }

    public static EmailLogResponse from(EmailLog emailLog, String senderName, String clientName) {
        return new EmailLogResponse(
                emailLog.getEmailLogId(),
                emailLog.getClientId(),
                clientName,
                emailLog.getPoId(),
                emailLog.getEmailTitle(),
                emailLog.getEmailRecipientName(),
                emailLog.getEmailRecipientEmail(),
                emailLog.getEmailSenderId(),
                emailLog.getEmailStatus(),
                emailLog.getEmailSentAt(),
                emailLog.getCreatedAt(),
                emailLog.getDocTypes() != null
                        ? emailLog.getDocTypes().stream().map(EmailLogTypeResponse::from).toList()
                        : List.of(),
                senderName
        );
    }
}
