package com.team2.activity.command.service;

import com.team2.activity.command.repository.EmailLogRepository;
import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.enums.MailStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 이메일 로그 쓰기 유스케이스를 담당하는 command service다.
@Service
// final 필드 기반 생성자 주입을 자동 생성한다.
@RequiredArgsConstructor
// 쓰기 작업이 하나의 트랜잭션으로 처리되도록 보장한다.
@Transactional
public class EmailLogCommandService {

    // 이메일 로그 저장소 접근을 담당한다.
    private final EmailLogRepository emailLogRepository;

    // 새 이메일 로그를 저장한다.
    public EmailLog createEmailLog(EmailLog emailLog) {
        // 전달받은 이메일 로그 엔티티를 저장소에 저장한다.
        return emailLogRepository.save(emailLog);
    }

    // 실패 상태의 이메일 로그를 재전송 상태로 되돌린다.
    public EmailLog resend(Long emailLogId) {
        // 재전송 대상 이메일 로그를 먼저 조회한다.
        EmailLog emailLog = findById(emailLogId);
        // 이미 발송 성공 상태면 중복 재전송을 막는다.
        if (emailLog.getEmailStatus() == MailStatus.SENT) {
            throw new IllegalStateException("이미 발송된 이메일입니다.");
        }
        // 실패 상태를 SENT로 바꿔 재전송 처리 결과를 반영한다.
        emailLog.updateStatus(MailStatus.SENT);
        // 상태가 바뀐 엔티티를 그대로 반환한다.
        return emailLog;
    }

    // 이메일 로그 상태를 임의의 상태로 변경한다.
    public EmailLog updateStatus(Long emailLogId, MailStatus emailStatus) {
        // 상태를 바꿀 이메일 로그를 먼저 조회한다.
        EmailLog emailLog = findById(emailLogId);
        // 요청한 상태 값으로 엔티티 상태를 갱신한다.
        emailLog.updateStatus(emailStatus);
        // 변경 감지 대상 엔티티를 그대로 반환한다.
        return emailLog;
    }

    // 이메일 로그를 조회한 뒤 삭제한다.
    public void deleteEmailLog(Long emailLogId) {
        // 삭제 대상 이메일 로그를 먼저 조회한다.
        EmailLog emailLog = findById(emailLogId);
        // 조회한 이메일 로그를 저장소에서 삭제한다.
        emailLogRepository.delete(emailLog);
    }

    // ID로 이메일 로그를 조회하고 없으면 예외를 던진다.
    private EmailLog findById(Long emailLogId) {
        return emailLogRepository.findById(emailLogId)
                // 조회 결과가 없으면 이메일 로그 없음 예외를 던진다.
                .orElseThrow(() -> new IllegalArgumentException("이메일 로그를 찾을 수 없습니다."));
    }
}
