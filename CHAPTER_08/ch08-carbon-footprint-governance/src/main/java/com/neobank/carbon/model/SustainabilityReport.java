package com.neobank.carbon.model;

import java.util.List;

public class SustainabilityReport {
    
    private String accountId;
    private String period;
    private Double totalCO2Kg;
    private Integer totalTransactions;
    private Double averageCO2PerTransaction;
    private Transaction highestImpactTransaction;
    private Double offsetPercentage;
    private Double esgAverageScore;
    private List<String> recommendations;
    private PeriodComparison comparison;
    
    public SustainabilityReport() {}
    
    public SustainabilityReport(String accountId, String period, Double totalCO2Kg,
                                Integer totalTransactions, Double averageCO2PerTransaction,
                                Transaction highestImpactTransaction, Double offsetPercentage,
                                Double esgAverageScore, List<String> recommendations,
                                PeriodComparison comparison) {
        this.accountId = accountId;
        this.period = period;
        this.totalCO2Kg = totalCO2Kg;
        this.totalTransactions = totalTransactions;
        this.averageCO2PerTransaction = averageCO2PerTransaction;
        this.highestImpactTransaction = highestImpactTransaction;
        this.offsetPercentage = offsetPercentage;
        this.esgAverageScore = esgAverageScore;
        this.recommendations = recommendations;
        this.comparison = comparison;
    }
    
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Double getTotalCO2Kg() { return totalCO2Kg; }
    public void setTotalCO2Kg(Double totalCO2Kg) { this.totalCO2Kg = totalCO2Kg; }
    public Integer getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }
    public Double getAverageCO2PerTransaction() { return averageCO2PerTransaction; }
    public void setAverageCO2PerTransaction(Double averageCO2PerTransaction) { this.averageCO2PerTransaction = averageCO2PerTransaction; }
    public Transaction getHighestImpactTransaction() { return highestImpactTransaction; }
    public void setHighestImpactTransaction(Transaction highestImpactTransaction) { this.highestImpactTransaction = highestImpactTransaction; }
    public Double getOffsetPercentage() { return offsetPercentage; }
    public void setOffsetPercentage(Double offsetPercentage) { this.offsetPercentage = offsetPercentage; }
    public Double getEsgAverageScore() { return esgAverageScore; }
    public void setEsgAverageScore(Double esgAverageScore) { this.esgAverageScore = esgAverageScore; }
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    public PeriodComparison getComparison() { return comparison; }
    public void setComparison(PeriodComparison comparison) { this.comparison = comparison; }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private String accountId, period;
        private Double totalCO2Kg, averageCO2PerTransaction, offsetPercentage, esgAverageScore;
        private Integer totalTransactions;
        private Transaction highestImpactTransaction;
        private List<String> recommendations;
        private PeriodComparison comparison;
        
        public Builder accountId(String accountId) { this.accountId = accountId; return this; }
        public Builder period(String period) { this.period = period; return this; }
        public Builder totalCO2Kg(Double totalCO2Kg) { this.totalCO2Kg = totalCO2Kg; return this; }
        public Builder totalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; return this; }
        public Builder averageCO2PerTransaction(Double averageCO2PerTransaction) { this.averageCO2PerTransaction = averageCO2PerTransaction; return this; }
        public Builder highestImpactTransaction(Transaction highestImpactTransaction) { this.highestImpactTransaction = highestImpactTransaction; return this; }
        public Builder offsetPercentage(Double offsetPercentage) { this.offsetPercentage = offsetPercentage; return this; }
        public Builder esgAverageScore(Double esgAverageScore) { this.esgAverageScore = esgAverageScore; return this; }
        public Builder recommendations(List<String> recommendations) { this.recommendations = recommendations; return this; }
        public Builder comparison(PeriodComparison comparison) { this.comparison = comparison; return this; }
        
        public SustainabilityReport build() {
            return new SustainabilityReport(accountId, period, totalCO2Kg, totalTransactions,
                                           averageCO2PerTransaction, highestImpactTransaction,
                                           offsetPercentage, esgAverageScore, recommendations, comparison);
        }
    }
}
