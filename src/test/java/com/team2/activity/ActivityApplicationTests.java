package com.team2.activity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// 스프링 애플리케이션 컨텍스트가 테스트 프로파일로 정상 기동되는지 확인한다.
@SpringBootTest
@ActiveProfiles("test")
class ActivityApplicationTests {

    @Test
    void contextLoads() {
        // 예외 없이 컨텍스트가 올라오면 테스트는 성공한다.
    }
}
