package com.neobank.users.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LenderProfile {
    private Double totalLent;
    private Integer activeLoans;
    private Double averageReturn;
    private RiskTolerance riskTolerance;
    private Boolean verified;
    
    public enum RiskTolerance {
        CONSERVATIVE, MODERATE, AGGRESSIVE
    }
}
