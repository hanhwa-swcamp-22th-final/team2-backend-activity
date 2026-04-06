package com.team2.activity.command.infrastructure.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "거래처 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    @Schema(description = "거래처 ID")
    private Integer id;
    @Schema(description = "거래처명 (영문)")
    private String clientName;
    @Schema(description = "거래처명 (한글)")
    private String clientNameKr;
    @Schema(description = "거래처 코드")
    private String clientCode;

    public String getName() {
        return clientName;
    }
}
