package com.neobank.carbon.model;

public class CarbonFootprint {
    
    private Double co2Kg;
    private Double treesEquivalent;
    private ImpactLevel impactLevel;
    private String calculationMethod;
    private Boolean offsetPurchased;
    private Double offsetCost;
    private CarbonBreakdown breakdown;
    
    public CarbonFootprint() {}
    
    public CarbonFootprint(Double co2Kg, Double treesEquivalent, ImpactLevel impactLevel,
                           String calculationMethod, Boolean offsetPurchased,
                           Double offsetCost, CarbonBreakdown breakdown) {
        this.co2Kg = co2Kg;
        this.treesEquivalent = treesEquivalent;
        this.impactLevel = impactLevel;
        this.calculationMethod = calculationMethod;
        this.offsetPurchased = offsetPurchased;
        this.offsetCost = offsetCost;
        this.breakdown = breakdown;
    }
    
    public Double getCo2Kg() { return co2Kg; }
    public void setCo2Kg(Double co2Kg) { this.co2Kg = co2Kg; }
    public Double getTreesEquivalent() { return treesEquivalent; }
    public void setTreesEquivalent(Double treesEquivalent) { this.treesEquivalent = treesEquivalent; }
    public ImpactLevel getImpactLevel() { return impactLevel; }
    public void setImpactLevel(ImpactLevel impactLevel) { this.impactLevel = impactLevel; }
    public String getCalculationMethod() { return calculationMethod; }
    public void setCalculationMethod(String calculationMethod) { this.calculationMethod = calculationMethod; }
    public Boolean getOffsetPurchased() { return offsetPurchased; }
    public void setOffsetPurchased(Boolean offsetPurchased) { this.offsetPurchased = offsetPurchased; }
    public Double getOffsetCost() { return offsetCost; }
    public void setOffsetCost(Double offsetCost) { this.offsetCost = offsetCost; }
    public CarbonBreakdown getBreakdown() { return breakdown; }
    public void setBreakdown(CarbonBreakdown breakdown) { this.breakdown = breakdown; }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private Double co2Kg, treesEquivalent, offsetCost;
        private ImpactLevel impactLevel;
        private String calculationMethod;
        private Boolean offsetPurchased;
        private CarbonBreakdown breakdown;
        
        public Builder co2Kg(Double co2Kg) { this.co2Kg = co2Kg; return this; }
        public Builder treesEquivalent(Double treesEquivalent) { this.treesEquivalent = treesEquivalent; return this; }
        public Builder impactLevel(ImpactLevel impactLevel) { this.impactLevel = impactLevel; return this; }
        public Builder calculationMethod(String calculationMethod) { this.calculationMethod = calculationMethod; return this; }
        public Builder offsetPurchased(Boolean offsetPurchased) { this.offsetPurchased = offsetPurchased; return this; }
        public Builder offsetCost(Double offsetCost) { this.offsetCost = offsetCost; return this; }
        public Builder breakdown(CarbonBreakdown breakdown) { this.breakdown = breakdown; return this; }
        
        public CarbonFootprint build() {
            return new CarbonFootprint(co2Kg, treesEquivalent, impactLevel, calculationMethod,
                                      offsetPurchased, offsetCost, breakdown);
        }
    }
}
