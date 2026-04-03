package com.team2.activity.query.controller;

import com.team2.activity.command.infrastructure.client.ClientResponse;
import com.team2.activity.command.infrastructure.client.DocumentsFeignClient;
import com.team2.activity.command.infrastructure.client.MasterFeignClient;
import com.team2.activity.command.infrastructure.client.PurchaseOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProxyController {

    private final MasterFeignClient masterFeignClient;
    private final DocumentsFeignClient documentsFeignClient;

    @GetMapping("/clients")
    public ResponseEntity<List<ClientResponse>> getClients(
            @RequestParam(required = false) String keyword) {
        try {
            return ResponseEntity.ok(masterFeignClient.getClients(keyword));
        } catch (Exception e) {
            log.warn("거래처 목록 프록시 조회 실패 [keyword={}]: {}", keyword, e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/pos")
    public ResponseEntity<List<PurchaseOrderResponse>> getPurchaseOrders(
            @RequestParam(value = "client_id", required = false) Long clientId,
            @RequestParam(value = "date_from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "date_to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        try {
            String from = dateFrom != null ? dateFrom.toString() : null;
            String to = dateTo != null ? dateTo.toString() : null;
            return ResponseEntity.ok(documentsFeignClient.getPurchaseOrders(clientId, from, to));
        } catch (Exception e) {
            log.warn("PO 목록 프록시 조회 실패 [clientId={}]: {}", clientId, e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }
}
