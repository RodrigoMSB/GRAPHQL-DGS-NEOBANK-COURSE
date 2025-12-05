package com.neobank.carbon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarbonFootprint {
    
    private Double co2Kg;
    private Double treesEquivalent;
    private ImpactLevel impactLevel;
    private String calculationMethod;
    private Boolean offsetPurchased;
    private Double offsetCost;
    private CarbonBreakdown breakdown;
}
