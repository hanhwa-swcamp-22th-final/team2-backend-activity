package com.team2.activity.command.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "documents-service",
        url = "${documents.service.url:http://localhost:8014}",
        fallbackFactory = DocumentsFeignFallbackFactory.class
)
public interface DocumentsFeignClient {

    // 발주 목록을 전체 조회한다. (사용자 쿼리 → Bearer 전파)
    @GetMapping("/api/purchase-orders")
    List<PurchaseOrderResponse> getPurchaseOrders();

    // PO 코드로 발주 정보를 조회한다. (사용자 쿼리 → Bearer 전파)
    @GetMapping("/api/purchase-orders/{poId}")
    PurchaseOrderResponse getPurchaseOrder(@PathVariable("poId") String poId);

    // 일반 메일 발송 — Documents 가 Activity 에 로그를 기록한다. (사용자 트리거 → Bearer 전파)
    @PostMapping("/api/emails/send")
    EmailSendResponse sendEmail(@RequestBody EmailSendRequest request);

    /**
     * 재전송 전용 — Documents 는 로그를 기록하지 않는다.
     * Activity 가 기존 EmailLog 의 상태를 직접 갱신하므로 Documents 의 logToActivity 호출이 없어야
     * email_logs 테이블에 중복 row 가 생기지 않는다.
     *
     * 경로에 {@code /internal} 을 포함시켜:
     *   - Gateway 에서 denyAll 로 외부 완전 차단
     *   - InternalTokenFeignInterceptor 가 X-Internal-Token 자동 주입
     *   - FeignAuthorizationConfig 는 Bearer 전파 생략
     *
     * X-User-Id 헤더로 원본 emailLog 의 sender userId 를 Documents 에 전달.
     */
    @PostMapping("/api/emails/internal/send-no-log")
    EmailSendResponse sendEmailWithoutLogging(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody EmailSendRequest request);
}
