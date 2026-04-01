package com.team2.activity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// 스프링 부트 자동 설정과 컴포넌트 스캔을 시작하는 애플리케이션 진입점이다.
@SpringBootApplication
@EnableFeignClients(basePackages = "com.team2.activity.command.infrastructure.client")
public class ActivityApplication {

    // JVM 실행 시 스프링 애플리케이션 컨텍스트를 부팅한다.
    public static void main(String[] args) {
        // 현재 애플리케이션 클래스를 기준으로 스프링 부트를 시작한다.
        SpringApplication.run(ActivityApplication.class, args);
    }
}
