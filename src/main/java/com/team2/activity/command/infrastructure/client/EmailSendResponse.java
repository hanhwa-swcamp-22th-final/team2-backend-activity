package com.team2.activity.command.infrastructure.client;

import java.util.List;

// document 서비스의 POST /emails/send 응답 DTO다.
public record EmailSendResponse(
        // 발송 결과 상태다 ("SENT" 또는 "FAILED").
        String status,
        // 발송 결과 메시지다.
        String message,
        // 첨부 파일 경로 목록이다.
        List<String> filePaths
) {}
