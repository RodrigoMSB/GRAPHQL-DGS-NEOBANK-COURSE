package com.neobank.carbon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    private String id;
    private String accountId;
    private Double amount;
    private String currency;
    private String merchantName;
    private MerchantCategory merchantCategory;
    private LocalDate date;
    
    // Calculated fields
    private CarbonFootprint carbonFootprint;
    private ESGScore esgScore;
    
    // @deprecated fields (mantained for backward compatibility)
    @Deprecated
    private String category; // Use merchantCategory instead
    
    @Deprecated
    private Boolean hasOffset; // Moved to CarbonFootprint.offsetPurchased
}
