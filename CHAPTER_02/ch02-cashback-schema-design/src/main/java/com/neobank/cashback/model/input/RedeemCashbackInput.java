package com.neobank.cashback.model.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedeemCashbackInput {
    private String userId;
    private Double amount;
    private String redemptionMethod;
}
