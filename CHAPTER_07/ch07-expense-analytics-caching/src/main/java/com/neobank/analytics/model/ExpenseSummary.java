package com.neobank.analytics.model;

import java.util.List;

public class ExpenseSummary {
    
    private Double totalAmount;
    private Double averageAmount;
    private Integer count;
    private Category category;
    private List<MerchantStats> topMerchants;
    
    public ExpenseSummary() {
    }
    
    public ExpenseSummary(Double totalAmount, Double averageAmount, Integer count,
                          Category category, List<MerchantStats> topMerchants) {
        this.totalAmount = totalAmount;
        this.averageAmount = averageAmount;
        this.count = count;
        this.category = category;
        this.topMerchants = topMerchants;
    }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public Double getAverageAmount() { return averageAmount; }
    public void setAverageAmount(Double averageAmount) { this.averageAmount = averageAmount; }
    
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public List<MerchantStats> getTopMerchants() { return topMerchants; }
    public void setTopMerchants(List<MerchantStats> topMerchants) { this.topMerchants = topMerchants; }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private Double totalAmount;
        private Double averageAmount;
        private Integer count;
        private Category category;
        private List<MerchantStats> topMerchants;
        
        public Builder totalAmount(Double totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder averageAmount(Double averageAmount) { this.averageAmount = averageAmount; return this; }
        public Builder count(Integer count) { this.count = count; return this; }
        public Builder category(Category category) { this.category = category; return this; }
        public Builder topMerchants(List<MerchantStats> topMerchants) { this.topMerchants = topMerchants; return this; }
        
        public ExpenseSummary build() {
            return new ExpenseSummary(totalAmount, averageAmount, count, category, topMerchants);
        }
    }
}
