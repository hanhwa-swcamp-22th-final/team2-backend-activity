package com.team2.activity.command.application.service;

import com.team2.activity.command.application.dto.EmailLogCreateRequest;
import com.team2.activity.command.application.dto.EmailLogInternalRequest;
import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.command.domain.repository.EmailLogRepository;
import com.team2.activity.command.infrastructure.client.DocumentsFeignClient;
import com.team2.activity.command.infrastructure.client.EmailSendRequest;
import com.team2.activity.command.infrastructure.client.EmailSendResponse;
import com.team2.activity.query.dto.EmailLogResponse;
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

    // 이메일 로그를 생성하고 응답 DTO를 반환한다.
    @Transactional
    public EmailLogResponse createEmailLog(EmailLogCreateRequest request, Long userId) {
        EmailLog emailLog = emailLogRepository.save(request.toEntity(userId));
        return EmailLogResponse.from(emailLog);
    }

    // document 서비스가 발송 후 activity 서비스로 로그를 전달할 때 저장한다.
    @Transactional
    public EmailLog createEmailLogInternal(EmailLogInternalRequest request) {
        return emailLogRepository.save(request.toEntity());
    }

    /**
     * FAILED 상태의 이메일 로그를 Documents 에 재전송 요청한다.
     *
     * <p><b>Write Ownership</b>: Activity 가 EmailLog 의 유일한 write owner 다.
     * Documents 는 {@code /api/emails/internal/send-no-log} 경로로 로그 기록 없이 발송만 수행한다.
     * 이로써 1회 재전송이 email_logs 테이블에 중복 row 를 만들지 않는다.
     *
     * <p><b>Race condition 방어</b>: 상태를 FAILED → SENDING 으로 원자적으로 전이 (DB UPDATE WHERE status='failed').
     * affected rows = 0 이면 이미 다른 요청이 진행 중이거나 상태가 바뀐 것이므로 409 계열 예외를 던진다.
     *
     * <p><b>결과 처리</b>:
     * <ul>
     *   <li>Documents 가 SENT 반환 → EmailLog SENT 로 전이 (markAsSent + 발송 시각)</li>
     *   <li>Documents 가 FAILED 반환 또는 예외 → EmailLog FAILED 로 전이 후 IllegalStateException 전파</li>
     * </ul>
     */
    @Transactional
    public EmailLogResponse resend(Long emailLogId, Long userId) {
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
        // 이미 재전송이 진행 중이면 중복 클릭이므로 거부한다 (빠른 경로 — DB UPDATE 없이 바로 실패).
        if (emailLog.getEmailStatus() == MailStatus.SENDING) {
            throw new IllegalStateException("이미 재전송이 진행 중입니다.");
        }

        // DB 레벨 원자적 전이: FAILED → SENDING.
        // 동시에 여러 요청이 들어와도 하나만 성공한다.
        int updated = emailLogRepository.transitionStatus(
                emailLogId, MailStatus.FAILED, MailStatus.SENDING);
        if (updated == 0) {
            // 경쟁에서 패배 → 이미 다른 요청이 SENDING 으로 바꿨거나 상태가 변경됨.
            throw new IllegalStateException("이미 재전송이 진행 중이거나 상태가 변경되었습니다.");
        }

        // JPA 1차 캐시의 엔티티 상태를 DB 와 동기화 (transitionStatus 가 벌크 update 이므로 flush + refresh)
        emailLog.markAsSending();

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
            // Documents 에 메일 발송만 위임 — 로그 기록은 이쪽에서만 수행 (이중 write 방지)
            // 원본 emailLog 의 sender userId 를 X-User-Id 헤더로 전달.
            EmailSendResponse response =
                    documentsFeignClient.sendEmailWithoutLogging(emailLog.getEmailSenderId(), sendRequest);

            // 발송 결과에 따라 이메일 로그 상태를 갱신한다.
            if ("SENT".equals(response.status())) {
                emailLog.markAsSent();
            } else {
                emailLog.markAsFailed();
                log.error("document 서비스 재전송 실패 응답 [emailLogId={}, status={}, message={}]",
                        emailLogId, response.status(), response.message());
            }
        } catch (Exception e) {
            // document 서비스 호출 실패 시 FAILED 상태로 전환한다.
            emailLog.markAsFailed();
            log.error("document 서비스 재전송 예외 [emailLogId={}, to={}]: {}",
                    emailLogId, emailLog.getEmailRecipientEmail(), e.getMessage(), e);
        }

        // 발송 결과가 FAILED면 재전송 실패 예외를 던진다.
        // (dirty checking 에 의해 transaction commit 시 DB 에 반영됨)
        if (emailLog.getEmailStatus() == MailStatus.FAILED) {
            throw new IllegalStateException("이메일 재전송에 실패했습니다.");
        }

        return EmailLogResponse.from(emailLog);
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
