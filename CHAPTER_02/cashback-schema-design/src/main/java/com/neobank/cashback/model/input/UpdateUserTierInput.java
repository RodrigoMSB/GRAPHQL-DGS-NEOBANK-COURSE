package com.neobank.cashback.model.input;

import com.neobank.cashback.model.CashbackTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserTierInput {
    private String userId;
    private CashbackTier newTier;
    private String reason;
}
