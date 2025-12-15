package com.neobank.carbon.model;

public class PeriodComparison {
    
    private String previousPeriod;
    private Double co2Change;
    private Double percentageChange;
    private Trend trend;
    
    public PeriodComparison() {}
    
    public PeriodComparison(String previousPeriod, Double co2Change,
                            Double percentageChange, Trend trend) {
        this.previousPeriod = previousPeriod;
        this.co2Change = co2Change;
        this.percentageChange = percentageChange;
        this.trend = trend;
    }
    
    public String getPreviousPeriod() { return previousPeriod; }
    public void setPreviousPeriod(String previousPeriod) { this.previousPeriod = previousPeriod; }
    public Double getCo2Change() { return co2Change; }
    public void setCo2Change(Double co2Change) { this.co2Change = co2Change; }
    public Double getPercentageChange() { return percentageChange; }
    public void setPercentageChange(Double percentageChange) { this.percentageChange = percentageChange; }
    public Trend getTrend() { return trend; }
    public void setTrend(Trend trend) { this.trend = trend; }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private String previousPeriod;
        private Double co2Change, percentageChange;
        private Trend trend;
        
        public Builder previousPeriod(String previousPeriod) { this.previousPeriod = previousPeriod; return this; }
        public Builder co2Change(Double co2Change) { this.co2Change = co2Change; return this; }
        public Builder percentageChange(Double percentageChange) { this.percentageChange = percentageChange; return this; }
        public Builder trend(Trend trend) { this.trend = trend; return this; }
        
        public PeriodComparison build() {
            return new PeriodComparison(previousPeriod, co2Change, percentageChange, trend);
        }
    }
}
