package com.team2.activity.command.application.dto;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.EmailLogType;
import com.team2.activity.command.domain.entity.enums.DocumentType;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// 이메일 로그 생성 요청 본문을 받는 DTO다.
@Schema(description = "이메일 로그 생성 요청")
public record EmailLogCreateRequest(
        @Schema(description = "거래처 ID", example = "1")
        @NotNull Long clientId,
        @Schema(description = "PO ID", example = "PO-001")
        String poId,
        @Schema(description = "이메일 제목", example = "견적서 송부")
        @NotBlank String emailTitle,
        @Schema(description = "수신자 이름", example = "김철수")
        String emailRecipientName,
        @Schema(description = "수신자 이메일", example = "kim@example.com")
        @NotBlank String emailRecipientEmail,
        @Schema(description = "첨부 문서 유형 목록")
        List<DocumentType> docTypes
) {
    // 요청 DTO를 EmailLog 엔티티로 변환한다.
    public EmailLog toEntity(Long userId) {
        // 문서 유형 목록이 없으면 빈 리스트로 초기화한다.
        List<EmailLogType> docTypeList = docTypes != null
                // 문서 유형 목록을 EmailLogType 엔티티 목록으로 변환한다.
                ? docTypes.stream().map(EmailLogType::of).toList()
                // 문서 유형이 없으면 빈 목록을 사용한다.
                : List.of();
        // EmailLog 빌더를 열어 요청 값을 엔티티 필드로 복사한다.
        return EmailLog.builder()
                // 거래처 ID를 엔티티에 복사한다.
                .clientId(clientId)
                // PO ID를 엔티티에 복사한다.
                .poId(poId)
                // 제목을 엔티티에 복사한다.
                .emailTitle(emailTitle)
                // 수신자 이름을 엔티티에 복사한다.
                .emailRecipientName(emailRecipientName)
                // 수신자 이메일을 엔티티에 복사한다.
                .emailRecipientEmail(emailRecipientEmail)
                // 요청 헤더 사용자 ID를 발송자로 저장한다.
                .emailSenderId(userId)
                // 발송 전 초기 상태를 PENDING으로 설정한다. 발송 성공 시 SENT, 실패 시 FAILED로 전환한다.
                .emailStatus(MailStatus.PENDING)
                // 문서 유형 목록을 엔티티에 연결한다.
                .docTypes(docTypeList)
                // 모든 필드 복사가 끝난 EmailLog 엔티티 생성을 마무리한다.
                .build();
    }
}
