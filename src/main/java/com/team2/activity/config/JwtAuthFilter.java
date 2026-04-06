package com.team2.activity.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JWT → X-User-Id 헤더 변환 필터.
 *
 * - 외부 요청: Authorization: Bearer <jwt> → JWT 검증 후 X-User-Id 헤더 주입
 * - 내부 서비스 간 호출: X-User-Id 헤더가 이미 있으면 신뢰 (Docker 내부 네트워크)
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final SecretKey secretKey;

    public JwtAuthFilter(@Value("${jwt.secret:myDefaultSecretKeyForDevelopmentMustBe256BitsLong!!}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String existingUserId = request.getHeader("X-User-Id");
        if (existingUserId != null && !existingUserId.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String userId = claims.getSubject();
                HttpServletRequest wrappedRequest = new HeaderInjectingRequestWrapper(request, Map.of(
                        "X-User-Id", userId,
                        "X-User-Email", Objects.toString(claims.get("email", String.class), ""),
                        "X-User-Name", Objects.toString(claims.get("name", String.class), ""),
                        "X-User-Role", Objects.toString(claims.get("role", String.class), "")
                ));
                filterChain.doFilter(wrappedRequest, response);
                return;
            } catch (Exception ignored) {
                // JWT 검증 실패 — 헤더 없이 통과 (permitAll 정책)
            }
        }

        filterChain.doFilter(request, response);
    }

    private static class HeaderInjectingRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String> injectedHeaders;

        HeaderInjectingRequestWrapper(HttpServletRequest request, Map<String, String> headers) {
            super(request);
            this.injectedHeaders = headers;
        }

        @Override
        public String getHeader(String name) {
            String injected = injectedHeaders.get(name);
            return injected != null ? injected : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String injected = injectedHeaders.get(name);
            if (injected != null) {
                return Collections.enumeration(List.of(injected));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new LinkedHashSet<>();
            Enumeration<String> original = super.getHeaderNames();
            while (original.hasMoreElements()) {
                names.add(original.nextElement());
            }
            names.addAll(injectedHeaders.keySet());
            return Collections.enumeration(names);
        }
    }
}
