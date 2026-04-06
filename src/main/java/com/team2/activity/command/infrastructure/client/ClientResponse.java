package com.team2.activity.command.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Master 서비스는 camelCase(clientName, clientNameKr, clientCode)로 응답한다.
// @JsonAlias는 역직렬화 시 추가 이름을 허용하므로 직렬화(SNAKE_CASE 출력)에는 영향을 주지 않는다.
// HATEOAS _links 등 알 수 없는 필드는 무시한다.
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "거래처 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    @Schema(description = "거래처 ID")
    private Integer id;

    // Master 응답 키 clientName 또는 SNAKE_CASE client_name 모두 수용한다.
    @Schema(description = "거래처명 (영문)")
    @JsonAlias("clientName")
    private String clientName;

    // Master 응답 키 clientNameKr 또는 SNAKE_CASE client_name_kr 모두 수용한다.
    @Schema(description = "거래처명 (한글)")
    @JsonAlias("clientNameKr")
    private String clientNameKr;

    // Master 응답 키 clientCode 또는 SNAKE_CASE client_code 모두 수용한다.
    @Schema(description = "거래처 코드")
    @JsonAlias("clientCode")
    private String clientCode;

    public String getName() {
        return clientName;
    }
}
