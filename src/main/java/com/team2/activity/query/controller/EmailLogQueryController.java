package com.team2.activity.query.controller;

import com.team2.activity.common.PagedResponse;
import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.enums.MailStatus;
import com.team2.activity.query.service.EmailLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 이메일 로그 읽기 API를 제공하는 query controller다.
@RestController
// 이메일 로그 조회 엔드포인트 기본 경로를 지정한다.
@RequestMapping("/api/email-logs")
// 생성자 주입용 생성자를 자동 생성한다.
@RequiredArgsConstructor
public class EmailLogQueryController {

    // 이메일 로그 조회 로직을 서비스에 위임한다.
    private final EmailLogQueryService emailLogQueryService;

    // 조건에 맞는 이메일 로그 목록을 조회해 페이징 응답으로 반환한다.
    @GetMapping
    public ResponseEntity<PagedResponse<EmailLog>> getEmailLogs(
            // 거래처 ID 조건이다.
            @RequestParam(required = false) Long clientId,
            // 상태 조건이다.
            @RequestParam(required = false) MailStatus emailStatus) {
        // 상황에 따라 사용할 조회 결과를 담을 변수다.
        List<EmailLog> logs;
        // 거래처 ID가 있으면 거래처 기준으로 먼저 조회한다.
        if (clientId != null) {
            // 거래처 조건 이메일 로그 목록을 조회한다.
            logs = emailLogQueryService.getEmailLogsByClientId(clientId);
        // 거래처 ID가 없고 상태가 있으면 상태 기준 조회를 수행한다.
        } else if (emailStatus != null) {
            // 상태 조건 이메일 로그 목록을 조회한다.
            logs = emailLogQueryService.getEmailLogsByStatus(emailStatus);
        // 조건이 없으면 전체 목록을 조회한다.
        } else {
            // 조건이 없으므로 전체 이메일 로그 목록을 조회한다.
            logs = emailLogQueryService.getAllEmailLogs();
        }
        // 조회된 목록을 단일 페이지 응답으로 감싸 200 OK로 반환한다.
        return ResponseEntity.ok(PagedResponse.of(logs));
    }

    // 이메일 로그 ID로 단건 상세를 조회한다.
    @GetMapping("/{emailLogId}")
    public ResponseEntity<EmailLog> getEmailLog(@PathVariable Long emailLogId) {
        // 서비스에서 조회한 이메일 로그 엔티티를 200 OK로 반환한다.
        return ResponseEntity.ok(emailLogQueryService.getEmailLog(emailLogId));
    }
}
