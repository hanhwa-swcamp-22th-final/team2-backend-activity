package com.team2.activity.command.infrastructure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DocumentsFeignFallbackFactory implements FallbackFactory<DocumentsFeignClient> {

    private static final Logger log = LoggerFactory.getLogger(DocumentsFeignFallbackFactory.class);

    @Override
    public DocumentsFeignClient create(Throwable cause) {
        return new DocumentsFeignClient() {
            @Override
            public List<PurchaseOrderResponse> getPurchaseOrders() {
                log.warn("[fallback] documents-service getPurchaseOrders unavailable: {}",
                        cause != null ? cause.getMessage() : "unknown");
                return Collections.emptyList();
            }

            @Override
            public PurchaseOrderResponse getPurchaseOrder(String poId) {
                log.warn("[fallback] documents-service getPurchaseOrder({}) unavailable: {}",
                        poId, cause != null ? cause.getMessage() : "unknown");
                return new PurchaseOrderResponse(poId, "(unknown)", "UNAVAILABLE", null);
            }

            @Override
            public EmailSendResponse sendEmail(EmailSendRequest request) {
                log.warn("[fallback] documents-service sendEmail unavailable: {}",
                        cause != null ? cause.getMessage() : "unknown");
                // EmailLogCommandService.java:69 의 response.status() NPE 방지를 위해
                // 반드시 non-null EmailSendResponse 객체 반환
                return new EmailSendResponse("FAILED", "documents service unavailable", Collections.emptyList());
            }

            @Override
            public EmailSendResponse sendEmailWithoutLogging(Long userId, EmailSendRequest request) {
                log.warn("[fallback] documents-service sendEmailWithoutLogging(userId={}) unavailable: {}",
                        userId, cause != null ? cause.getMessage() : "unknown");
                return new EmailSendResponse("FAILED", "documents service unavailable", Collections.emptyList());
            }
        };
    }
}
