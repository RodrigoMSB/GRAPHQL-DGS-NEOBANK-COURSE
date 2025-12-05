package com.neobank.fraud.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAlert {
    
    private String id;
    private Transaction transaction;
    private RiskLevel riskLevel;
    private List<String> reasons;
    private LocalDateTime detectedAt;
    private String recommendedAction;
}
