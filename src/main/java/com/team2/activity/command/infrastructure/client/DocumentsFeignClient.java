package com.team2.activity.command.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "documents-service",
        url = "${documents.service.url:http://localhost:8014}",
        fallbackFactory = DocumentsFeignFallbackFactory.class
)
public interface DocumentsFeignClient {

    // PO 코드로 발주 정보를 조회한다.
    @GetMapping("/api/purchase-orders/{poId}")
    PurchaseOrderResponse getPurchaseOrder(@PathVariable("poId") String poId);

    // document 서비스에 메일 발송을 요청한다. 재전송 시 사용한다. (Phase 4에서 Bearer 전파로 교체 예정)
    @PostMapping("/api/emails/send")
    EmailSendResponse sendEmail(
            @RequestBody EmailSendRequest request);
}
