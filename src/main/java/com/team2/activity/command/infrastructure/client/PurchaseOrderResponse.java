package com.team2.activity.command.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Documents 서비스는 camelCase(poId, poNo)로 응답한다.
// @JsonAlias는 역직렬화 시 추가 이름을 허용하므로 직렬화(SNAKE_CASE 출력)에는 영향을 주지 않는다.
// HATEOAS _links 및 Documents의 나머지 필드는 무시한다.
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "발주(PO) 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {

    // Documents 응답 키 poId 또는 SNAKE_CASE po_id 모두 수용한다.
    @Schema(description = "발주 ID")
    @JsonAlias("poId")
    private String poId;

    // Documents 응답 키 poNo 또는 SNAKE_CASE po_no 모두 수용한다.
    @Schema(description = "발주 번호")
    @JsonAlias("poNo")
    private String poNo;

    @Schema(description = "발주 진행 상태")
    private String status;
}
