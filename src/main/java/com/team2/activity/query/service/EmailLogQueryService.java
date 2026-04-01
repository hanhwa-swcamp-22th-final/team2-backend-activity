package com.team2.activity.query.service;

import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.enums.MailStatus;
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

    // 이메일 로그 ID로 단건을 조회하고 없으면 예외를 던진다.
    public EmailLog getEmailLog(Long emailLogId) {
        // mapper를 호출해 emailLogId에 해당하는 이메일 로그를 조회한다.
        EmailLog emailLog = emailLogQueryMapper.findById(emailLogId);
        // 조회 결과가 없으면 단건 조회 실패 예외를 던진다.
        if (emailLog == null) {
            throw new IllegalArgumentException("이메일 로그를 찾을 수 없습니다.");
        }
        // 조회된 이메일 로그 엔티티를 그대로 반환한다.
        return emailLog;
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
