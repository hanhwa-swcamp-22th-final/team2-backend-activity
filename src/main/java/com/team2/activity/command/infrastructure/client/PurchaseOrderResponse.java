package com.team2.activity.command.infrastructure.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 문서 서비스에서 내려주는 발주 정보 응답 모델이다.
@Schema(description = "발주(PO) 응답")
@Getter
// 역직렬화를 위해 기본 생성자를 제공한다.
@NoArgsConstructor
// 테스트나 수동 생성에 사용할 전체 필드 생성자를 제공한다.
@AllArgsConstructor
public class PurchaseOrderResponse {

    @Schema(description = "발주 ID")
    private String poId;
    @Schema(description = "발주 번호")
    private String poNo;
    @Schema(description = "발주 진행 상태")
    private String status;
}
