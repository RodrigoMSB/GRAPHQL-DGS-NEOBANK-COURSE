package com.neobank.carbon.service;

import com.neobank.carbon.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CarbonCalculatorService {
    
    private static final Logger log = LoggerFactory.getLogger(CarbonCalculatorService.class);
    
    public CarbonFootprint calculateFootprint(Double amount, MerchantCategory category, 
                                              String merchantName) {
        double baseFactor = getBaseFactor(category);
        double co2Kg = (amount / 100.0) * baseFactor;
        CarbonBreakdown breakdown = calculateBreakdown(co2Kg, category);
        ImpactLevel impactLevel = determineImpactLevel(co2Kg);
        double treesEquivalent = co2Kg / 20.0;
        
        log.debug("Carbon calculated: {} - {} kg CO2 ({})", 
                merchantName, String.format("%.2f", co2Kg), impactLevel);
        
        return CarbonFootprint.builder()
                .co2Kg(Double.parseDouble(String.format("%.2f", co2Kg)))
                .treesEquivalent(Double.parseDouble(String.format("%.2f", treesEquivalent)))
                .impactLevel(impactLevel)
                .calculationMethod("Category-Based Estimation v2.0")
                .offsetPurchased(false)
                .breakdown(breakdown)
                .build();
    }
    
    private double getBaseFactor(MerchantCategory category) {
        return switch (category) {
            case TRAVEL_AVIATION -> 120.0;
            case ENERGY -> 45.0;
            case TRANSPORTATION -> 35.0;
            case FASHION_RETAIL -> 25.0;
            case ELECTRONICS -> 20.0;
            case FOOD_RETAIL -> 8.0;
            case TRAVEL_HOSPITALITY -> 15.0;
            case SERVICES -> 5.0;
            case OTHER -> 10.0;
        };
    }
    
    private CarbonBreakdown calculateBreakdown(double totalCO2, MerchantCategory category) {
        double transportation = switch (category) {
            case TRAVEL_AVIATION, TRANSPORTATION -> totalCO2 * 0.7;
            case FASHION_RETAIL, ELECTRONICS -> totalCO2 * 0.4;
            default -> totalCO2 * 0.2;
        };
        
        double production = switch (category) {
            case ELECTRONICS -> totalCO2 * 0.5;
            case FASHION_RETAIL -> totalCO2 * 0.4;
            case FOOD_RETAIL -> totalCO2 * 0.6;
            default -> totalCO2 * 0.3;
        };
        
        double packaging = totalCO2 - transportation - production;
        
        return CarbonBreakdown.builder()
                .transportationCO2(Double.parseDouble(String.format("%.2f", transportation)))
                .productionCO2(Double.parseDouble(String.format("%.2f", production)))
                .packagingCO2(Double.parseDouble(String.format("%.2f", Math.max(0, packaging))))
                .notes("Estimated breakdown based on industry averages")
                .build();
    }
    
    private ImpactLevel determineImpactLevel(double co2Kg) {
        if (co2Kg < 5.0) return ImpactLevel.LOW;
        if (co2Kg < 20.0) return ImpactLevel.MEDIUM;
        if (co2Kg < 50.0) return ImpactLevel.HIGH;
        return ImpactLevel.CRITICAL;
    }
}
