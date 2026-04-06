package com.team2.activity.command.application.service;

import com.team2.activity.command.application.dto.EmailLogInternalRequest;
import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.command.domain.repository.EmailLogRepository;
import com.team2.activity.command.infrastructure.client.DocumentsFeignClient;
import com.team2.activity.command.infrastructure.client.EmailSendRequest;
import com.team2.activity.command.infrastructure.client.EmailSendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 이메일 로그 쓰기 유스케이스를 담당하는 command service다.
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailLogCommandService {

    private final EmailLogRepository emailLogRepository;
    // 재전송 시 실제 메일 발송을 document 서비스에 위임한다.
    private final DocumentsFeignClient documentsFeignClient;

    // 이메일 로그를 저장하고 반환한다.
    @Transactional
    public EmailLog createEmailLog(EmailLog emailLog) {
        return emailLogRepository.save(emailLog);
    }

    // document 서비스가 발송 후 activity 서비스로 로그를 전달할 때 저장한다.
    @Transactional
    public EmailLog createEmailLogInternal(EmailLogInternalRequest request) {
        return emailLogRepository.save(request.toEntity());
    }

    // FAILED 상태의 이메일 로그를 document 서비스를 통해 재전송한다.
    @Transactional
    public EmailLog resend(Long emailLogId, Long userId) {
        // 재전송 대상 이메일 로그를 조회한다.
        EmailLog emailLog = findById(emailLogId);
        // 이미 발송 성공한 이메일은 재전송 대상이 아니다.
        if (emailLog.getEmailStatus() == MailStatus.SENT) {
            throw new IllegalStateException("이미 발송된 이메일입니다.");
        }
        // 아직 발송 시도 전인 이메일도 재전송 대상이 아니다.
        if (emailLog.getEmailStatus() == MailStatus.PENDING) {
            throw new IllegalStateException("아직 발송 시도 전인 이메일입니다.");
        }

        // 기존 로그 데이터로 document 서비스 발송 요청을 구성한다.
        List<String> docTypeStrings = emailLog.getDocTypes().stream()
                .map(docType -> docType.getEmailDocType().getDisplayName())
                .toList();

        EmailSendRequest sendRequest = new EmailSendRequest(
                emailLog.getClientId(),
                emailLog.getPoId(),
                emailLog.getEmailTitle(),
                emailLog.getEmailRecipientName(),
                emailLog.getEmailRecipientEmail(),
                docTypeStrings
        );

        try {
            // document 서비스에 메일 발송을 위임한다.
            EmailSendResponse response = documentsFeignClient.sendEmail(userId, sendRequest);
            // 발송 결과에 따라 이메일 로그 상태를 갱신한다.
            if ("SENT".equals(response.status())) {
                emailLog.markAsSent();
            } else {
                emailLog.markAsFailed();
            }
        } catch (Exception e) {
            // document 서비스 호출 실패 시 FAILED 상태로 전환한다.
            emailLog.markAsFailed();
            log.error("document 서비스 재전송 실패 [emailLogId={}, to={}]: {}",
                    emailLogId, emailLog.getEmailRecipientEmail(), e.getMessage(), e);
        }

        // 발송 결과가 FAILED면 재전송 실패 예외를 던진다.
        if (emailLog.getEmailStatus() == MailStatus.FAILED) {
            throw new IllegalStateException("이메일 재전송에 실패했습니다.");
        }

        return emailLog;
    }

    // 이메일 로그를 삭제한다.
    @Transactional
    public void deleteEmailLog(Long emailLogId) {
        EmailLog emailLog = findById(emailLogId);
        emailLogRepository.delete(emailLog);
    }

    // ID로 이메일 로그를 조회하고 없으면 예외를 던진다.
    private EmailLog findById(Long emailLogId) {
        return emailLogRepository.findById(emailLogId)
                .orElseThrow(() -> new IllegalArgumentException("이메일 로그를 찾을 수 없습니다."));
    }
}
