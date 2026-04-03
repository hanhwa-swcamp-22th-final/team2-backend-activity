package com.team2.activity.command.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Integer id;
    private String clientName;
    private String clientNameKr;
    private String clientCode;

    public String getName() {
        return clientName;
    }
}
