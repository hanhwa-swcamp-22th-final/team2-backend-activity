package com.team2.activity.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// 컨트롤러 전역에서 발생한 예외를 공통 JSON 응답으로 변환한다.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 잘못된 입력이나 조회 실패를 404 응답으로 변환한다.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException e) {
        // 응답 상태 코드를 404로 설정한다.
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                // 예외 메시지를 message 키의 JSON 본문으로 담는다.
                .body(Map.of("message", e.getMessage()));
    }

    // 상태 충돌 상황을 409 응답으로 변환한다.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
        // 응답 상태 코드를 409로 설정한다.
        return ResponseEntity.status(HttpStatus.CONFLICT)
                // 예외 메시지를 message 키의 JSON 본문으로 담는다.
                .body(Map.of("message", e.getMessage()));
    }
}
