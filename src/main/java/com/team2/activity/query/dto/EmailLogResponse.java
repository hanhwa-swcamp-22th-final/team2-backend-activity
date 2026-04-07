package com.team2.activity.query.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "이메일 로그 응답")
public record EmailLogResponse(
        @Schema(description = "이메일 로그 ID") Long emailLogId,
        @Schema(description = "거래처 ID") Long clientId,
        @Schema(description = "거래처명") String clientName,
        @Schema(description = "PO ID") String poId,
        @Schema(description = "이메일 제목") String emailTitle,
        @Schema(description = "수신자 이름") String emailRecipientName,
        @Schema(description = "수신자 이메일") String emailRecipientEmail,
        @Schema(description = "발송자 ID") Long emailSenderId,
        @Schema(description = "메일 발송 상태") MailStatus emailStatus,
        @Schema(description = "발송 시각") LocalDateTime emailSentAt,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "첨부 문서 유형 목록") List<EmailLogTypeResponse> docTypes,
        @Schema(description = "발송자 이름") String senderName
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

    @JsonProperty("id")
    public Long id() { return emailLogId; }

    @JsonProperty("title")
    public String title() { return emailTitle; }

    @JsonProperty("email")
    public String email() { return emailRecipientEmail; }

    @JsonProperty("recipient")
    public String recipient() { return emailRecipientName; }

    @JsonProperty("sender")
    public String sender() { return senderName; }

    @JsonProperty("types")
    public List<EmailLogTypeResponse> types() { return docTypes; }

    @JsonProperty("status")
    public MailStatus status() { return emailStatus; }

    @JsonProperty("sentAt")
    public LocalDateTime sentAt() { return emailSentAt; }
}
