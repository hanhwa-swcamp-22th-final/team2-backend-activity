package com.team2.activity.dto;

import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.EmailLogType;
import com.team2.activity.entity.enums.DocumentType;
import com.team2.activity.entity.enums.MailStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// 이메일 로그 생성 요청 본문을 받는 DTO다.
public record EmailLogCreateRequest(
        // 이메일이 속한 거래처 ID다.
        @NotNull Long clientId,
        // 관련 PO ID다.
        String poId,
        // 이메일 제목이다.
        @NotBlank String emailTitle,
        // 수신자 이름이다.
        String emailRecipientName,
        // 수신자 이메일 주소다.
        @NotBlank String emailRecipientEmail,
        // 첨부 문서 유형 목록이다.
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
                // 생성 시 기본 상태를 SENT로 설정한다.
                .emailStatus(MailStatus.SENT)
                // 문서 유형 목록을 엔티티에 연결한다.
                .docTypes(docTypeList)
                // 모든 필드 복사가 끝난 EmailLog 엔티티 생성을 마무리한다.
                .build();
    }
}
