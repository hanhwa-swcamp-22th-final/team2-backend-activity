package com.team2.activity.command.infrastructure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthFeignFallbackFactory implements FallbackFactory<AuthFeignClient> {

    private static final Logger log = LoggerFactory.getLogger(AuthFeignFallbackFactory.class);

    @Override
    public AuthFeignClient create(Throwable cause) {
        return userId -> {
            log.warn("[fallback] auth-service getUser({}) unavailable: {}",
                    userId, cause != null ? cause.getMessage() : "unknown");
            // 호출부 NPE 방지를 위해 placeholder 객체 반환
            return new UserResponse(userId == null ? null : userId.intValue(), "(unknown user)", null);
        };
    }
}
