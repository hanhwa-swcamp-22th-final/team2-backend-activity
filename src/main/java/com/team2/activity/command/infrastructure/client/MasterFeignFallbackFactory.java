package com.team2.activity.command.infrastructure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MasterFeignFallbackFactory implements FallbackFactory<MasterFeignClient> {

    private static final Logger log = LoggerFactory.getLogger(MasterFeignFallbackFactory.class);

    @Override
    public MasterFeignClient create(Throwable cause) {
        return new MasterFeignClient() {
            @Override
            public ClientResponse getClient(Long clientId) {
                log.warn("[fallback] master-service getClient({}) unavailable: {}",
                        clientId, cause != null ? cause.getMessage() : "unknown");
                return new ClientResponse(
                        clientId == null ? null : clientId.intValue(),
                        "(unknown client)",
                        "(거래처 조회 실패)",
                        null
                );
            }

            @Override
            public List<ClientResponse> getClients() {
                log.warn("[fallback] master-service getClients() unavailable: {}",
                        cause != null ? cause.getMessage() : "unknown");
                return Collections.emptyList();
            }
        };
    }
}
