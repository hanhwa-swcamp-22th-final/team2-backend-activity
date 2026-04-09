package com.team2.activity.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 내부 전용 Feign 호출에 X-Internal-Token 헤더를 자동 주입한다 (Documents 서비스와 동일 규약).
 * 관례: 경로에 {@code /internal} 세그먼트가 포함된 호출을 시스템 호출로 간주한다.
 *
 * Activity 는 현재 시점에는 다른 서비스의 {@code /internal} 엔드포인트를 호출하지 않지만,
 * 향후 양방향 시스템 통신이 필요할 때(예: Activity → Documents 시스템 이벤트) 동일 규약으로
 * 동작하도록 대칭 구성한다.
 *
 * Bearer JWT 전파는 {@link FeignAuthorizationConfig} 에서 담당하며, 내부 경로에는
 * 전파하지 않도록 분리돼 있어 시스템/사용자 채널이 명확히 구분된다.
 */
@Configuration
public class InternalTokenFeignInterceptor {

    @Value("${internal.api.token:}")
    private String internalToken;

    @Bean
    public RequestInterceptor internalTokenRequestInterceptor() {
        return (RequestTemplate template) -> {
            if (!isInternalPath(template.url())) {
                return;
            }
            if (internalToken == null || internalToken.isBlank()) {
                return;
            }
            template.header("X-Internal-Token", internalToken);
        };
    }

    /** 내부 전용 경로 식별자. FeignAuthorizationConfig 와 동일 규칙. */
    static boolean isInternalPath(String url) {
        return url != null && url.contains("/internal");
    }
}
