package com.neobank.analytics.model;

public class MonthComparison {
    
    private MonthlyAnalytics month1;
    private MonthlyAnalytics month2;
    private Double difference;
    private Double percentageChange;
    
    public MonthComparison() {
    }
    
    public MonthComparison(MonthlyAnalytics month1, MonthlyAnalytics month2,
                           Double difference, Double percentageChange) {
        this.month1 = month1;
        this.month2 = month2;
        this.difference = difference;
        this.percentageChange = percentageChange;
    }
    
    public MonthlyAnalytics getMonth1() { return month1; }
    public void setMonth1(MonthlyAnalytics month1) { this.month1 = month1; }
    
    public MonthlyAnalytics getMonth2() { return month2; }
    public void setMonth2(MonthlyAnalytics month2) { this.month2 = month2; }
    
    public Double getDifference() { return difference; }
    public void setDifference(Double difference) { this.difference = difference; }
    
    public Double getPercentageChange() { return percentageChange; }
    public void setPercentageChange(Double percentageChange) { this.percentageChange = percentageChange; }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private MonthlyAnalytics month1;
        private MonthlyAnalytics month2;
        private Double difference;
        private Double percentageChange;
        
        public Builder month1(MonthlyAnalytics month1) { this.month1 = month1; return this; }
        public Builder month2(MonthlyAnalytics month2) { this.month2 = month2; return this; }
        public Builder difference(Double difference) { this.difference = difference; return this; }
        public Builder percentageChange(Double percentageChange) { this.percentageChange = percentageChange; return this; }
        
        public MonthComparison build() {
            return new MonthComparison(month1, month2, difference, percentageChange);
        }
    }
}
