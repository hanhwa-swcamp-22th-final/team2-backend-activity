package com.team2.activity.command.service;

import com.team2.activity.command.repository.EmailLogRepository;
import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.enums.MailStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailLogCommandService {

    private final EmailLogRepository emailLogRepository;

    public EmailLog createEmailLog(EmailLog emailLog) {
        return emailLogRepository.save(emailLog);
    }

    public EmailLog resend(Long emailLogId) {
        EmailLog emailLog = findById(emailLogId);
        if (emailLog.getEmailStatus() == MailStatus.SENT) {
            throw new IllegalStateException("이미 발송된 이메일입니다.");
        }
        emailLog.updateStatus(MailStatus.SENT);
        return emailLog;
    }

    public EmailLog updateStatus(Long emailLogId, MailStatus emailStatus) {
        EmailLog emailLog = findById(emailLogId);
        emailLog.updateStatus(emailStatus);
        return emailLog;
    }

    public void deleteEmailLog(Long emailLogId) {
        EmailLog emailLog = findById(emailLogId);
        emailLogRepository.delete(emailLog);
    }

    private EmailLog findById(Long emailLogId) {
        return emailLogRepository.findById(emailLogId)
                .orElseThrow(() -> new IllegalArgumentException("이메일 로그를 찾을 수 없습니다."));
    }
}
