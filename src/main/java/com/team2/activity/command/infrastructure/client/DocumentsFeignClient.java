package com.team2.activity.command.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "documents-service", url = "${documents.service.url:http://localhost:8084}")
public interface DocumentsFeignClient {

    // PO 코드로 발주 정보를 조회한다.
    @GetMapping("/api/purchase-orders/{poId}")
    PurchaseOrderResponse getPurchaseOrder(@PathVariable("poId") String poId);

    // document 서비스에 메일 발송을 요청한다. 재전송 시 사용한다.
    @PostMapping("/emails/send")
    EmailSendResponse sendEmail(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody EmailSendRequest request);
}
