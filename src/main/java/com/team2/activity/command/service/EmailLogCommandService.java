package com.team2.activity.command.service;

import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.enums.MailStatus;
import com.team2.activity.command.repository.EmailLogRepository;
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
