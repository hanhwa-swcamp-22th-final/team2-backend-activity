package com.team2.activity.command.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 인증 서비스에서 내려주는 사용자 정보 응답 모델이다.
@Getter
// 역직렬화를 위해 기본 생성자를 제공한다.
@NoArgsConstructor
// 테스트나 수동 생성에 사용할 전체 필드 생성자를 제공한다.
@AllArgsConstructor
public class UserResponse {

    // 사용자 고유 식별자를 저장한다.
    private Long id;
    // 화면에 노출할 사용자 이름을 저장한다.
    private String name;
    // 사용자 이메일 주소를 저장한다.
    private String email;
}
