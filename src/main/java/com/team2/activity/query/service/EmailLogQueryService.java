package com.team2.activity.query.service;

import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.enums.MailStatus;
import com.team2.activity.query.mapper.EmailLogQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailLogQueryService {

    private final EmailLogQueryMapper emailLogQueryMapper;

    public EmailLog getEmailLog(Long emailLogId) {
        EmailLog emailLog = emailLogQueryMapper.findById(emailLogId);
        if (emailLog == null) {
            throw new IllegalArgumentException("이메일 로그를 찾을 수 없습니다.");
        }
        return emailLog;
    }

    public List<EmailLog> getAllEmailLogs() {
        return emailLogQueryMapper.findAll();
    }

    public List<EmailLog> getEmailLogsByClientId(Long clientId) {
        return emailLogQueryMapper.findByClientId(clientId);
    }

    public List<EmailLog> getEmailLogsByStatus(MailStatus emailStatus) {
        return emailLogQueryMapper.findByEmailStatus(emailStatus);
    }
}
