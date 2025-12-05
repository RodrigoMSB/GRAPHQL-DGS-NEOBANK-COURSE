package com.neobank.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthComparison {
    
    private MonthlyAnalytics month1;
    private MonthlyAnalytics month2;
    private Double difference;
    private Double percentageChange;
}
