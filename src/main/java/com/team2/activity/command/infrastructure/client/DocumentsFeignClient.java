package com.team2.activity.command.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "documents-service", url = "${documents.service.url:http://localhost:8084}")
public interface DocumentsFeignClient {

    @GetMapping("/api/purchase-orders/{poId}")
    PurchaseOrderResponse getPurchaseOrder(@PathVariable("poId") String poId);

    @GetMapping("/api/documents/pdf/download")
    byte[] downloadPdf(@RequestParam("s3Key") String s3Key);
}
