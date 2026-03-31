package com.team2.activity.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {

    private String poId;
    private String poNo;
    private String status;
}
