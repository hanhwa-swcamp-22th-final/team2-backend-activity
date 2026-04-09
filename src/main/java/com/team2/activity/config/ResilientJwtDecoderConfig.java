package com.team2.activity.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Resilient JwtDecoder — JWKS endpoint 가 일시 장애여도 인증이 유지되도록 2-tier fallback 제공.
 *
 * <p><b>동작</b>:
 * <ol>
 *   <li>Primary: {@code spring.security.oauth2.resourceserver.jwt.jwk-set-uri} 를 사용하는
 *       표준 {@link NimbusJwtDecoder}</li>
 *   <li>Fallback: {@code jwt.fallback-public-key-path} 가 설정돼 있으면 해당 경로의 PEM
 *       (PKCS#8 SubjectPublicKeyInfo) 를 읽어 로컬 RSA 공개키 기반 NimbusJwtDecoder 를 구성</li>
 * </ol>
 *
 * <p><b>운영 관점</b>:
 * <ul>
 *   <li>Auth 서비스 다운 시에도 Fallback 공개키로 JWT 검증 지속</li>
 *   <li>key rotation 시에는 Auth 새 키 배포 → 각 서비스 ConfigMap 갱신 → Pod rolling</li>
 *   <li>k8s 에서는 {@code JWT_FALLBACK_PUBLIC_KEY_PATH=file:/etc/team2/jwt/public.pem} 같이
 *       Secret/ConfigMap 볼륨 마운트 경로 지정</li>
 * </ul>
 *
 * <p><b>비활성화</b>: {@code jwt.fallback-public-key-path} 가 비어있거나 미지정이면
 * 기존 Spring Boot auto-config 가 만드는 NimbusJwtDecoder 를 그대로 사용 (backward compat).
 * 본 Bean 은 resolve 되지 않으면 auto-config 가 override 되지 않는다.
 */
@Configuration
public class ResilientJwtDecoderConfig {

    private static final Logger log = LoggerFactory.getLogger(ResilientJwtDecoderConfig.class);

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${jwt.fallback-public-key-path:}")
    private String fallbackPublicKeyPath;

    /**
     * Fallback 경로가 설정된 경우에만 커스텀 JwtDecoder 를 Bean 으로 등록.
     * 그렇지 않으면 null 을 반환해 Spring Boot auto-config 가 기본 NimbusJwtDecoder 를 만든다.
     */
    @Bean
    public JwtDecoder jwtDecoder(ResourceLoader resourceLoader) {
        NimbusJwtDecoder primary = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        if (fallbackPublicKeyPath == null || fallbackPublicKeyPath.isBlank()) {
            log.info("ResilientJwtDecoder: fallback disabled — primary NimbusJwtDecoder only");
            return primary;
        }

        JwtDecoder fallback = buildFallbackDecoder(resourceLoader, fallbackPublicKeyPath);
        log.info("ResilientJwtDecoder: fallback enabled with public key at {}", fallbackPublicKeyPath);
        return new ResilientJwtDecoder(primary, fallback);
    }

    private JwtDecoder buildFallbackDecoder(ResourceLoader loader, String path) {
        try {
            Resource resource = loader.getResource(path);
            try (InputStream is = resource.getInputStream()) {
                byte[] raw = is.readAllBytes();
                String pem = new String(raw, StandardCharsets.UTF_8)
                        .replaceAll("-----[A-Z ]+-----", "")
                        .replaceAll("\\s", "");
                byte[] der = Base64.getDecoder().decode(pem);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(spec);
                return NimbusJwtDecoder.withPublicKey(publicKey).build();
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to load JWT fallback public key from: " + path, e);
        }
    }

    /** Primary 실패 시 fallback 으로 재시도. */
    static class ResilientJwtDecoder implements JwtDecoder {
        private static final Logger log = LoggerFactory.getLogger(ResilientJwtDecoder.class);
        private final JwtDecoder primary;
        private final JwtDecoder fallback;

        ResilientJwtDecoder(JwtDecoder primary, JwtDecoder fallback) {
            this.primary = primary;
            this.fallback = fallback;
        }

        @Override
        public Jwt decode(String token) throws JwtException {
            try {
                return primary.decode(token);
            } catch (JwtException primaryEx) {
                log.warn("Primary JwtDecoder (JWKS) failed, attempting local fallback: {}",
                        primaryEx.getMessage());
                try {
                    return fallback.decode(token);
                } catch (JwtException fallbackEx) {
                    log.error("Both primary and fallback JwtDecoder failed. primary={} fallback={}",
                            primaryEx.getMessage(), fallbackEx.getMessage());
                    throw fallbackEx;
                }
            }
        }
    }
}
