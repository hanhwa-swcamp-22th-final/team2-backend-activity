package com.team2.activity.command.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.EmailLogAttachment;
import com.team2.activity.command.domain.entity.EmailLogType;
import com.team2.activity.command.domain.entity.enums.DocumentType;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

// document 서비스가 메일 발송 후 activity 서비스로 로그를 전달하는 내부 요청 DTO다.
// Documents 서비스는 SNAKE_CASE 설정이 없으므로 camelCase로 전송한다.
// @JsonAlias로 camelCase 입력을 추가 허용해 역직렬화 실패를 방지한다.
@Schema(description = "내부 이메일 로그 생성 요청 (Documents 서비스에서 호출)")
public record EmailLogInternalRequest(
        @JsonAlias("clientId") @Schema(description = "거래처 ID") Long clientId,
        @JsonAlias("poId") @Schema(description = "PO ID") String poId,
        @JsonAlias("emailTitle") @Schema(description = "이메일 제목") String emailTitle,
        @JsonAlias("emailRecipientName") @Schema(description = "수신자 이름") String emailRecipientName,
        @JsonAlias("emailRecipientEmail") @Schema(description = "수신자 이메일") String emailRecipientEmail,
        @JsonAlias("emailSenderId") @Schema(description = "발송자 ID") Long emailSenderId,
        @JsonAlias("emailStatus") @Schema(description = "발송 상태 (SENT/FAILED)") String emailStatus,
        @JsonAlias("docTypes") @Schema(description = "문서 유형 목록") List<String> docTypes,
        @JsonAlias("filePaths") @Schema(description = "첨부파일 경로 목록") List<String> filePaths,
        @JsonAlias("attachmentFilenames") @Schema(description = "첨부파일명 목록") List<String> attachmentFilenames
) {
    // 내부 요청 DTO를 EmailLog 엔티티로 변환한다.
    public EmailLog toEntity() {
        // docTypes 문자열 목록을 EmailLogType 엔티티 목록으로 변환한다.
        List<EmailLogType> docTypeList = docTypes != null
                ? docTypes.stream()
                    .map(type -> EmailLogType.of(DocumentType.from(type)))
                    .toList()
                : List.of();

        // attachmentFilenames와 filePaths를 순서대로 짝지어 EmailLogAttachment 목록을 구성한다.
        List<EmailLogAttachment> attachmentList = new ArrayList<>();
        if (attachmentFilenames != null) {
            for (int i = 0; i < attachmentFilenames.size(); i++) {
                String filename = attachmentFilenames.get(i);
                String filePath = (filePaths != null && i < filePaths.size()) ? filePaths.get(i) : null;
                attachmentList.add(EmailLogAttachment.of(filename, filePath));
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
