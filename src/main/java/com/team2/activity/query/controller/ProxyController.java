package com.team2.activity.query.controller;

import com.team2.activity.command.infrastructure.client.ClientResponse;
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

    @GetMapping("/clients")
    public ResponseEntity<List<ClientResponse>> getClients(
            @RequestParam(required = false) String keyword) {
        try {
            List<ClientResponse> clients = masterFeignClient.getClients();
            if (keyword != null && !keyword.isBlank()) {
                String lowerKeyword = keyword.toLowerCase();
                clients = clients.stream()
                        .filter(c -> (c.getClientName() != null && c.getClientName().toLowerCase().contains(lowerKeyword))
                                || (c.getClientNameKr() != null && c.getClientNameKr().contains(keyword)))
                        .toList();
            }
            return ResponseEntity.ok(clients);
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
        // documents 서비스 PO 목록 API 미구현 상태 — 서비스 구현 후 Feign 연동 예정
        log.info("PO 목록 프록시 요청 [clientId={}, dateFrom={}, dateTo={}] — documents 서비스 미구현", clientId, dateFrom, dateTo);
        return ResponseEntity.ok(List.of());
    }
}
