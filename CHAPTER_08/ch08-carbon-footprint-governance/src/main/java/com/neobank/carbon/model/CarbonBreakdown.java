package com.neobank.carbon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarbonBreakdown {
    
    private Double transportationCO2;
    private Double productionCO2;
    private Double packagingCO2;
    private String notes;
}
