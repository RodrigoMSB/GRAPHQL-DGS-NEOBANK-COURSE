package com.neobank.analytics.model;

public class CategoryBreakdown {
    
    private Category category;
    private Double amount;
    private Double percentage;
    
    public CategoryBreakdown() {
    }
    
    public CategoryBreakdown(Category category, Double amount, Double percentage) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
    }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private Category category;
        private Double amount;
        private Double percentage;
        
        public Builder category(Category category) { this.category = category; return this; }
        public Builder amount(Double amount) { this.amount = amount; return this; }
        public Builder percentage(Double percentage) { this.percentage = percentage; return this; }
        
        public CategoryBreakdown build() {
            return new CategoryBreakdown(category, amount, percentage);
        }
    }
}
