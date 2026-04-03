package com.team2.activity.command.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "documents-service", url = "${documents.service.url:http://localhost:8084}")
public interface DocumentsFeignClient {

    @GetMapping("/api/purchase-orders/{poId}")
    PurchaseOrderResponse getPurchaseOrder(@PathVariable("poId") String poId);
}
