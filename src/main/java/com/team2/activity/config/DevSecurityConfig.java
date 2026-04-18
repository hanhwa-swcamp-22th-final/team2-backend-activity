package com.team2.activity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// dev 환경에서만 활성화되어 모든 요청을 인증 없이 허용하는 Security 설정이다.
// 다만 /api/**\/internal/** 는 prod 와 동일하게 InternalApiTokenFilter 로 보호해야
// dev 환경에서도 내부 엔드포인트가 완전 개방되지 않는다 (blank 토큰이면 filter 가 통과시킴).
@Configuration
@EnableWebSecurity
@Profile("dev")
@Order(1)
public class DevSecurityConfig {

    private final InternalApiTokenFilter internalApiTokenFilter;

    public DevSecurityConfig(InternalApiTokenFilter internalApiTokenFilter) {
        this.internalApiTokenFilter = internalApiTokenFilter;
    }

    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(internalApiTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
