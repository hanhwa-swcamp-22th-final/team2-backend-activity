package com.team2.activity.command.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// 인증 서비스와의 HTTP 통신을 담당하는 Feign 클라이언트 인터페이스다.
@FeignClient(
        name = "auth-service",
        url = "${auth.service.url:http://localhost:8011}",
        fallbackFactory = AuthFeignFallbackFactory.class
)
public interface AuthFeignClient {

    // 사용자 ID로 인증 서비스에서 사용자 정보를 조회한다.
    // 일반 /api/users/{id} 는 hasRole(ADMIN) 이라 SALES 세션에서 403 → enrich 전체 실패.
    // InternalTokenFeignInterceptor 가 자동 주입하는 /internal 경로로 고정 (Documents 와 동일 규칙).
    @GetMapping("/api/users/internal/{userId}")
    // 경로 변수의 userId 값을 사용자 조회 키로 전달한다.
    UserResponse getUser(@PathVariable("userId") Long userId);
}
