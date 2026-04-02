package com.team2.activity.command.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 거래처 마스터 서비스와의 HTTP 통신을 담당하는 Feign 클라이언트 인터페이스다.
@FeignClient(name = "master-service", url = "${master.service.url:http://localhost:8082}")
public interface MasterFeignClient {

    // 거래처 ID로 마스터 서비스에서 거래처 정보를 조회한다.
    @GetMapping("/api/clients/{clientId}")
    // 경로 변수의 clientId 값을 거래처 조회 키로 전달한다.
    ClientResponse getClient(@PathVariable("clientId") Long clientId);
}
