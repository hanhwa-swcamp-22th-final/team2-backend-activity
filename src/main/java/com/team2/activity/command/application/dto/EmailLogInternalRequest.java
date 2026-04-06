package com.team2.activity.command.application.dto;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.EmailLogAttachment;
import com.team2.activity.command.domain.entity.EmailLogType;
import com.team2.activity.command.domain.entity.enums.DocumentType;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

// document 서비스가 메일 발송 후 activity 서비스로 로그를 전달하는 내부 요청 DTO다.
@Schema(description = "내부 이메일 로그 생성 요청 (Documents 서비스에서 호출)")
public record EmailLogInternalRequest(
        @Schema(description = "거래처 ID") Long clientId,
        @Schema(description = "PO ID") String poId,
        @Schema(description = "이메일 제목") String emailTitle,
        @Schema(description = "수신자 이름") String emailRecipientName,
        @Schema(description = "수신자 이메일") String emailRecipientEmail,
        @Schema(description = "발송자 ID") Long emailSenderId,
        @Schema(description = "발송 상태 (SENT/FAILED)") String emailStatus,
        @Schema(description = "문서 유형 목록") List<String> docTypes,
        @Schema(description = "S3 키 목록") List<String> s3Keys,
        @Schema(description = "첨부파일명 목록") List<String> attachmentFilenames
) {
    // 내부 요청 DTO를 EmailLog 엔티티로 변환한다.
    public EmailLog toEntity() {
        // docTypes 문자열 목록을 EmailLogType 엔티티 목록으로 변환한다.
        List<EmailLogType> docTypeList = docTypes != null
                ? docTypes.stream()
                    .map(type -> EmailLogType.of(DocumentType.from(type)))
                    .toList()
                : List.of();

        // attachmentFilenames와 s3Keys를 순서대로 짝지어 EmailLogAttachment 목록을 구성한다.
        List<EmailLogAttachment> attachmentList = new ArrayList<>();
        if (attachmentFilenames != null) {
            for (int i = 0; i < attachmentFilenames.size(); i++) {
                String filename = attachmentFilenames.get(i);
                String s3Key = (s3Keys != null && i < s3Keys.size()) ? s3Keys.get(i) : null;
                attachmentList.add(EmailLogAttachment.of(filename, s3Key));
            }
        }

        // 문자열 상태값을 MailStatus enum으로 변환한다.
        MailStatus status = MailStatus.from(emailStatus);

        return EmailLog.builder()
                .clientId(clientId)
                .poId(poId)
                .emailTitle(emailTitle)
                .emailRecipientName(emailRecipientName)
                .emailRecipientEmail(emailRecipientEmail)
                .emailSenderId(emailSenderId)
                .emailStatus(status)
                .docTypes(docTypeList)
                .attachments(attachmentList)
                .build();
    }
}
