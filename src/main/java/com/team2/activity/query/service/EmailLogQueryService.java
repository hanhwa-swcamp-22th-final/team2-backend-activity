package com.team2.activity.query.service;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import com.team2.activity.command.infrastructure.client.AuthFeignClient;
import com.team2.activity.command.infrastructure.client.UserResponse;
import com.team2.activity.query.dto.EmailLogResponse;
import com.team2.activity.query.mapper.EmailLogQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 이메일 로그 읽기 유스케이스를 담당하는 query service다.
@Service
// final 필드 기반 생성자 주입을 자동 생성한다.
@RequiredArgsConstructor
// 읽기 전용 트랜잭션으로 조회 성격을 명확히 한다.
@Transactional(readOnly = true)
public class EmailLogQueryService {

    // 이메일 로그 조회용 MyBatis mapper다.
    private final EmailLogQueryMapper emailLogQueryMapper;
    // 발송자 이름 조회를 위한 인증 서비스 Feign 클라이언트다.
    private final AuthFeignClient authFeignClient;

    // 이메일 로그 ID로 단건을 조회하고 발송자 이름을 enrichment해서 반환한다.
    public EmailLogResponse getEmailLog(Long emailLogId) {
        // mapper를 호출해 emailLogId에 해당하는 이메일 로그를 조회한다.
        EmailLog emailLog = emailLogQueryMapper.findById(emailLogId);
        // 조회 결과가 없으면 단건 조회 실패 예외를 던진다.
        if (emailLog == null) {
            throw new IllegalArgumentException("이메일 로그를 찾을 수 없습니다.");
        }
        // 인증 서비스에서 발송자 이름을 조회한다 (서비스 오류 시 null을 반환한다).
        String senderName = fetchUserName(emailLog.getEmailSenderId());
        // 엔티티와 발송자 이름을 합쳐 응답 DTO를 생성한다.
        return EmailLogResponse.from(emailLog, senderName);
    }

    // 인증 서비스에서 사용자 이름을 안전하게 조회한다.
    private String fetchUserName(Long userId) {
        // userId가 없으면 조회하지 않는다.
        if (userId == null) return null;
        try {
            // 인증 서비스에 사용자 정보를 요청한다.
            UserResponse user = authFeignClient.getUser(userId);
            // 응답이 있으면 이름을 반환하고, 없으면 null을 반환한다.
            return user != null ? user.getName() : null;
        } catch (Exception e) {
            // 인증 서비스가 응답하지 않아도 조회 전체가 실패하지 않도록 null을 반환한다.
            return null;
        }
    }

    // 전체 이메일 로그 목록을 조회한다.
    public List<EmailLog> getAllEmailLogs() {
        // 전체 이메일 로그 목록 조회를 mapper에 위임한다.
        return emailLogQueryMapper.findAll();
    }

    // 거래처 ID로 이메일 로그 목록을 조회한다.
    public List<EmailLog> getEmailLogsByClientId(Long clientId) {
        // 거래처 조건 목록 조회를 mapper에 위임한다.
        return emailLogQueryMapper.findByClientId(clientId);
    }

    // 상태 값으로 이메일 로그 목록을 조회한다.
    public List<EmailLog> getEmailLogsByStatus(MailStatus emailStatus) {
        // 상태 조건 목록 조회를 mapper에 위임한다.
        return emailLogQueryMapper.findByEmailStatus(emailStatus);
    }
}
