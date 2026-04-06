package com.team2.activity.command.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "master-service", url = "${master.service.url:http://localhost:8012}")
public interface MasterFeignClient {

    @GetMapping("/api/clients/{clientId}")
    ClientResponse getClient(@PathVariable("clientId") Long clientId);

    @GetMapping("/api/clients")
    List<ClientResponse> getClients();
}
