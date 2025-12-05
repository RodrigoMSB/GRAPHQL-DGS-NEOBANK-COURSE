package com.neobank.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantStats {
    
    private String merchantName;
    private Double totalSpent;
    private Integer transactionCount;
}
