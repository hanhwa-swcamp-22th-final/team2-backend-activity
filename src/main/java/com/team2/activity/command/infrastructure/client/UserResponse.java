package com.team2.activity.command.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Auth 서비스는 camelCase(userId, userName, userEmail)로 응답한다.
// @JsonAlias는 역직렬화 시 추가 이름을 허용하므로 직렬화(SNAKE_CASE 출력)에는 영향을 주지 않는다.
// HATEOAS _links 등 알 수 없는 필드는 무시한다.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    // Auth 응답 키 userId 또는 SNAKE_CASE user_id 모두 수용한다.
    @JsonAlias("userId")
    private Integer id;

    // Auth 응답 키 userName 또는 SNAKE_CASE user_name 모두 수용한다.
    @JsonAlias("userName")
    private String name;

    // Auth 응답 키 userEmail 또는 SNAKE_CASE user_email 모두 수용한다.
    @JsonAlias("userEmail")
    private String email;
}
