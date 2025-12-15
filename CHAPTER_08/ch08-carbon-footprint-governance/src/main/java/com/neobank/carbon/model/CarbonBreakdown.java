package com.neobank.carbon.model;

public class CarbonBreakdown {
    
    private Double transportationCO2;
    private Double productionCO2;
    private Double packagingCO2;
    private String notes;
    
    public CarbonBreakdown() {}
    
    public CarbonBreakdown(Double transportationCO2, Double productionCO2,
                           Double packagingCO2, String notes) {
        this.transportationCO2 = transportationCO2;
        this.productionCO2 = productionCO2;
        this.packagingCO2 = packagingCO2;
        this.notes = notes;
    }
    
    public Double getTransportationCO2() { return transportationCO2; }
    public void setTransportationCO2(Double transportationCO2) { this.transportationCO2 = transportationCO2; }
    public Double getProductionCO2() { return productionCO2; }
    public void setProductionCO2(Double productionCO2) { this.productionCO2 = productionCO2; }
    public Double getPackagingCO2() { return packagingCO2; }
    public void setPackagingCO2(Double packagingCO2) { this.packagingCO2 = packagingCO2; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private Double transportationCO2, productionCO2, packagingCO2;
        private String notes;
        
        public Builder transportationCO2(Double transportationCO2) { this.transportationCO2 = transportationCO2; return this; }
        public Builder productionCO2(Double productionCO2) { this.productionCO2 = productionCO2; return this; }
        public Builder packagingCO2(Double packagingCO2) { this.packagingCO2 = packagingCO2; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        
        public CarbonBreakdown build() {
            return new CarbonBreakdown(transportationCO2, productionCO2, packagingCO2, notes);
        }
    }
}
