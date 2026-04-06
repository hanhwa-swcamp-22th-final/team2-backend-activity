package com.team2.activity.command.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

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
) {}
