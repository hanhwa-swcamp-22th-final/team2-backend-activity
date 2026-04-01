package com.team2.activity.command.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 문서 서비스와의 HTTP 통신을 담당하는 Feign 클라이언트 인터페이스다.
@FeignClient(name = "documents-service", url = "${documents.service.url:http://localhost:8084}")
public interface DocumentsFeignClient {

    // PO 식별자로 문서 서비스에서 발주 정보를 조회한다.
    @GetMapping("/api/purchase-orders/{poId}")
    // 경로 변수의 poId 값을 문서 조회 키로 전달한다.
    PurchaseOrderResponse getPurchaseOrder(@PathVariable("poId") String poId);
}
