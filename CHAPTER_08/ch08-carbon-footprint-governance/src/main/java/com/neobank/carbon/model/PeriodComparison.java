package com.neobank.carbon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodComparison {
    
    private String previousPeriod;
    private Double co2Change;
    private Double percentageChange;
    private Trend trend;
}
