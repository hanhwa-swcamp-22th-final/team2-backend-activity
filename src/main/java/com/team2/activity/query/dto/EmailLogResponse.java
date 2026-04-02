package com.team2.activity.query.dto;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.EmailLogType;
import com.team2.activity.command.domain.entity.enums.MailStatus;

import java.time.LocalDateTime;
import java.util.List;

// 이메일 로그 API 응답에 사용하는 DTO record다.
public record EmailLogResponse(
        // 이메일 로그 고유 식별자다.
        Long emailLogId,
        // 연관 거래처 ID다.
        Long clientId,
        // 연관 PO ID다.
        String poId,
        // 이메일 제목이다.
        String emailTitle,
        // 수신자 이름이다.
        String emailRecipientName,
        // 수신자 이메일 주소다.
        String emailRecipientEmail,
        // 발송자 사용자 ID다.
        Long emailSenderId,
        // 발송 상태 enum이다.
        MailStatus emailStatus,
        // 실제 발송 완료 시각이다.
        LocalDateTime emailSentAt,
        // 레코드 생성 시각이다.
        LocalDateTime createdAt,
        // 첨부 문서 유형 목록이다.
        List<EmailLogType> docTypes,
        // 인증 서비스에서 조회한 발송자 이름이다 (상세 조회 시 채워진다).
        String senderName
) {
    // 목록 조회 등 외부 서비스 없이 엔티티만으로 DTO를 생성할 때 사용하는 팩터리 메서드다.
    public static EmailLogResponse from(EmailLog emailLog) {
        // senderName을 null로 두고 나머지 엔티티 값만 복사한다.
        return from(emailLog, null);
    }

    // 상세 조회 시 발송자 이름을 함께 받아 DTO를 생성하는 팩터리 메서드다.
    public static EmailLogResponse from(EmailLog emailLog, String senderName) {
        // 엔티티 각 필드를 레코드 생성자에 순서대로 넘겨 DTO를 구성한다.
        return new EmailLogResponse(
                // 이메일 로그 식별자를 복사한다.
                emailLog.getEmailLogId(),
                // 거래처 ID를 복사한다.
                emailLog.getClientId(),
                // PO ID를 복사한다.
                emailLog.getPoId(),
                // 이메일 제목을 복사한다.
                emailLog.getEmailTitle(),
                // 수신자 이름을 복사한다.
                emailLog.getEmailRecipientName(),
                // 수신자 이메일을 복사한다.
                emailLog.getEmailRecipientEmail(),
                // 발송자 ID를 복사한다.
                emailLog.getEmailSenderId(),
                // 발송 상태를 복사한다.
                emailLog.getEmailStatus(),
                // 발송 시각을 복사한다.
                emailLog.getEmailSentAt(),
                // 생성 시각을 복사한다.
                emailLog.getCreatedAt(),
                // 문서 유형 목록을 복사한다.
                emailLog.getDocTypes(),
                // 외부 서비스에서 조회한 발송자 이름을 설정한다.
                senderName
        );
    }
}
