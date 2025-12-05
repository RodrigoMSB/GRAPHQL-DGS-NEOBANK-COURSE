package com.neobank.carbon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SustainabilityReport {
    
    private String accountId;
    private String period;
    private Double totalCO2Kg;
    private Integer totalTransactions;
    private Double averageCO2PerTransaction;
    private Transaction highestImpactTransaction;
    private Double offsetPercentage;
    private Double esgAverageScore;
    private List<String> recommendations;
    private PeriodComparison comparison;
}
