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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailLogQueryService {

    private final EmailLogQueryMapper emailLogQueryMapper;
    private final AuthFeignClient authFeignClient;
    private final MasterFeignClient masterFeignClient;

    public EmailLogResponse getEmailLog(Long emailLogId) {
        EmailLog emailLog = emailLogQueryMapper.findEmailLogById(emailLogId);
        if (emailLog == null) {
            throw new IllegalArgumentException("이메일 로그를 찾을 수 없습니다.");
        }
        String senderName = fetchUserName(emailLog.getEmailSenderId());
        String clientName = fetchClientName(emailLog.getClientId());
        return EmailLogResponse.from(emailLog, senderName, clientName);
    }

    public List<EmailLogResponse> getEmailLogsWithFilters(Long clientId, String poId, MailStatus emailStatus,
                                                           Long emailSenderId, String keyword,
                                                           LocalDateTime dateFrom, LocalDateTime dateTo,
                                                           int page, int size) {
        int offset = page * size;
        List<EmailLog> logs = emailLogQueryMapper.findEmailLogsWithFilters(
                clientId, poId, emailStatus, emailSenderId, keyword, dateFrom, dateTo, size, offset);
        return enrichEmailLogs(logs);
    }

    public long countWithFilters(Long clientId, String poId, MailStatus emailStatus,
                                  Long emailSenderId, String keyword,
                                  LocalDateTime dateFrom, LocalDateTime dateTo) {
        return emailLogQueryMapper.countEmailLogsWithFilters(clientId, poId, emailStatus, emailSenderId, keyword, dateFrom, dateTo);
    }

    private List<EmailLogResponse> enrichEmailLogs(List<EmailLog> logs) {
        Set<Long> senderIds = logs.stream().map(EmailLog::getEmailSenderId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> clientIds = logs.stream().map(EmailLog::getClientId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> senderNames = new HashMap<>();
        senderIds.forEach(id -> senderNames.put(id, fetchUserName(id)));

        Map<Long, String> clientNames = new HashMap<>();
        clientIds.forEach(id -> clientNames.put(id, fetchClientName(id)));

        return logs.stream().map(e -> EmailLogResponse.from(e,
                senderNames.get(e.getEmailSenderId()),
                clientNames.get(e.getClientId())
        )).toList();
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
        return emailLogQueryMapper.findAllEmailLogs();
    }

    public List<EmailLog> getEmailLogsByClientId(Long clientId) {
        return emailLogQueryMapper.findEmailLogByClientId(clientId);
    }

    public List<EmailLog> getEmailLogsByStatus(MailStatus emailStatus) {
        return emailLogQueryMapper.findEmailLogByEmailStatus(emailStatus);
    }
}
