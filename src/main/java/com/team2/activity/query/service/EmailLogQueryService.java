package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.command.infrastructure.client.AuthFeignClient;
import com.team2.activity.command.infrastructure.client.ClientResponse;
import com.team2.activity.command.infrastructure.client.MasterFeignClient;
import com.team2.activity.command.infrastructure.client.UserResponse;
import com.team2.activity.query.dto.EmailLogResponse;
import com.team2.activity.query.mapper.EmailLogQueryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailLogQueryService {

    private final EmailLogQueryMapper emailLogQueryMapper;
    private final AuthFeignClient authFeignClient;
    private final MasterFeignClient masterFeignClient;

    public EmailLogResponse getEmailLog(Long emailLogId) {
        EmailLog emailLog = emailLogQueryMapper.findById(emailLogId);
        if (emailLog == null) {
            throw new IllegalArgumentException("이메일 로그를 찾을 수 없습니다.");
        }
        String senderName = fetchUserName(emailLog.getEmailSenderId());
        String clientName = fetchClientName(emailLog.getClientId());
        return EmailLogResponse.from(emailLog, senderName, clientName);
    }

    public List<EmailLogResponse> getEmailLogsWithFilters(Long clientId, String poId, MailStatus emailStatus,
                                                           Long emailSenderId, String keyword,
                                                           LocalDateTime dateFrom, LocalDateTime dateTo) {
        List<EmailLog> logs = emailLogQueryMapper.findWithFilters(
                clientId, poId, emailStatus, emailSenderId, keyword, dateFrom, dateTo);
        return logs.stream().map(this::enrichEmailLog).toList();
    }

    private EmailLogResponse enrichEmailLog(EmailLog emailLog) {
        String senderName = fetchUserName(emailLog.getEmailSenderId());
        String clientName = fetchClientName(emailLog.getClientId());
        return EmailLogResponse.from(emailLog, senderName, clientName);
    }

    private String fetchUserName(Long userId) {
        if (userId == null) return null;
        try {
            UserResponse user = authFeignClient.getUser(userId);
            return user != null ? user.getName() : null;
        } catch (Exception e) {
            log.warn("발송자 이름 조회 실패 [userId={}]: {}", userId, e.getMessage());
            return null;
        }
    }

    private String fetchClientName(Long clientId) {
        if (clientId == null) return null;
        try {
            ClientResponse client = masterFeignClient.getClient(clientId);
            return client != null ? client.getName() : null;
        } catch (Exception e) {
            log.warn("거래처명 조회 실패 [clientId={}]: {}", clientId, e.getMessage());
            return null;
        }
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
