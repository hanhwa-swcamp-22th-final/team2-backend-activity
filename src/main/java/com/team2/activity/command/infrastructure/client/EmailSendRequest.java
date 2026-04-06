package com.team2.activity.command.infrastructure.client;

import java.util.List;

// document 서비스의 POST /emails/send 에 전달하는 요청 DTO다.
public record EmailSendRequest(
        // 거래처 ID다.
        Long clientId,
        // PO 문서 코드다.
        String poId,
        // 이메일 제목이다.
        String emailTitle,
        // 수신자 이름이다.
        String emailRecipientName,
        // 수신자 이메일 주소다.
        String emailRecipientEmail,
        // 첨부할 문서 유형 목록이다 (PI, CI, PL, SHIPPING_ORDER, PRODUCTION_ORDER).
        List<String> docTypes
) {}
