package com.neobank.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummary {
    
    private Double totalAmount;
    private Double averageAmount;
    private Integer count;
    private Category category;
    private List<MerchantStats> topMerchants;
}
