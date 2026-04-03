package com.team2.activity.command.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "documents-service", url = "${documents.service.url:http://localhost:8084}")
public interface DocumentsFeignClient {

    @GetMapping("/api/purchase-orders/{poId}")
    PurchaseOrderResponse getPurchaseOrder(@PathVariable("poId") String poId);

    @GetMapping("/api/purchase-orders")
    List<PurchaseOrderResponse> getPurchaseOrders(
            @RequestParam(value = "client_id", required = false) Long clientId,
            @RequestParam(value = "date_from", required = false) String dateFrom,
            @RequestParam(value = "date_to", required = false) String dateTo);
}
