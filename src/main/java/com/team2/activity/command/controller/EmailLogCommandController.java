package com.team2.activity.command.controller;

import com.team2.activity.command.service.EmailLogCommandService;
import com.team2.activity.dto.EmailLogCreateRequest;
import com.team2.activity.entity.EmailLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 이메일 로그 쓰기 API를 제공하는 command controller다.
@RestController
// 이메일 로그 관련 command 엔드포인트 기본 경로를 지정한다.
@RequestMapping("/api/email-logs")
// 생성자 주입용 생성자를 자동 생성한다.
@RequiredArgsConstructor
public class EmailLogCommandController {

    // 이메일 로그 쓰기 로직을 서비스에 위임한다.
    private final EmailLogCommandService emailLogCommandService;

    // 이메일 로그 생성 요청을 받아 저장된 로그를 반환한다.
    @PostMapping
    public ResponseEntity<EmailLog> createEmailLog(
            // 헤더에서 현재 사용자 ID를 발송자로 받는다.
            @RequestHeader("X-User-Id") Long userId,
            // 요청 본문을 검증한 뒤 DTO로 바인딩한다.
            @Valid @RequestBody EmailLogCreateRequest request) {
        // 응답 상태 코드를 201 Created로 설정한다.
        return ResponseEntity.status(HttpStatus.CREATED)
                // DTO를 엔티티로 바꿔 서비스에 전달한다.
                .body(emailLogCommandService.createEmailLog(request.toEntity(userId)));
    }

    // 실패한 이메일 로그를 재전송 처리한다.
    @PostMapping("/{emailLogId}/resend")
    public ResponseEntity<EmailLog> resend(@PathVariable Long emailLogId) {
        // 재전송 결과 엔티티를 그대로 응답으로 반환한다.
        return ResponseEntity.ok(emailLogCommandService.resend(emailLogId));
    }
}
