package com.team2.activity.query.controller;

import com.team2.activity.command.infrastructure.client.ClientResponse;
import com.team2.activity.command.infrastructure.client.DocumentsFeignClient;
import com.team2.activity.command.infrastructure.client.MasterFeignClient;
import com.team2.activity.command.infrastructure.client.PurchaseOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "프록시", description = "외부 서비스 프록시 조회 API")
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProxyController {

    private final MasterFeignClient masterFeignClient;
    private final DocumentsFeignClient documentsFeignClient;

    @Operation(summary = "거래처 목록 조회", description = "마스터 서비스에서 거래처 목록을 프록시 조회한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/clients")
    public ResponseEntity<List<ClientResponse>> getClients(
            @Parameter(description = "거래처명 검색 키워드") @RequestParam(name = "keyword", required = false) String keyword) {
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

    @Operation(summary = "발주 목록 조회", description = "문서 서비스에서 발주(PO) 목록을 프록시 조회한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/pos")
    public ResponseEntity<List<PurchaseOrderResponse>> getPurchaseOrders(
            @Parameter(description = "거래처 ID") @RequestParam(value = "client_id", required = false) Long clientId) {
        try {
            List<PurchaseOrderResponse> pos = documentsFeignClient.getPurchaseOrders();
            if (clientId != null) {
                pos = pos.stream()
                        .filter(po -> clientId.equals(po.getClientId()))
                        .toList();
            }
            return ResponseEntity.ok(pos);
        } catch (Exception e) {
            log.warn("PO 목록 프록시 조회 실패 [clientId={}]: {}", clientId, e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }
}
