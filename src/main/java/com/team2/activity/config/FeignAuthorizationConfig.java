package com.team2.activity.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Feign 요청의 서비스 간 통신 인증 정책
 * ─────────────────────────────────────────────────────────
 * [원칙]
 *   1) 사용자 트리거 호출(일반 API) → 현재 SecurityContext 의 Bearer JWT 를 그대로 전파
 *      → 수신 서비스에서 동일한 사용자 권한으로 RBAC 평가 (감사 추적 + defense in depth)
 *   2) 시스템 호출(내부 전용 API) → Bearer 를 보내지 않고 X-Internal-Token 만 전송
 *      (주입은 {@link InternalTokenFeignInterceptor} 에서 담당)
 *      → 사용자 컨텍스트와 무관하게 동작, 스케줄러/배치/이벤트에서도 안전
 *
 * Activity 의 현재 외향 Feign 호출(AuthFeignClient/MasterFeignClient/DocumentsFeignClient)은
 * 모두 사용자 쿼리/트리거 맥락이므로 Bearer 전파 대상이다. 미래에 시스템 전용 호출이 추가되면
 * 경로에 {@code /internal} 을 포함시키는 것만으로 자동으로 올바른 채널을 타게 된다.
 */
@Configuration
public class FeignAuthorizationConfig {

    @Bean
    public RequestInterceptor bearerTokenForwardingInterceptor() {
        return template -> {
            if (isInternalPath(template.url())) {
                return;
            }
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                template.header("Authorization", "Bearer " + jwt.getTokenValue());
            }
        };
    }

    /** 내부 전용 경로 식별자. 경로에 {@code /internal} 세그먼트 포함. */
    static boolean isInternalPath(String url) {
        return url != null && url.contains("/internal");
    }
}
