package com.neobank.analytics.model;

import java.util.List;

public class MonthlyAnalytics {
    
    private String month;
    private Double totalSpent;
    private List<CategoryBreakdown> byCategory;
    private Expense topExpense;
    
    public MonthlyAnalytics() {
    }
    
    public MonthlyAnalytics(String month, Double totalSpent, List<CategoryBreakdown> byCategory, Expense topExpense) {
        this.month = month;
        this.totalSpent = totalSpent;
        this.byCategory = byCategory;
        this.topExpense = topExpense;
    }
    
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    
    public Double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }
    
    public List<CategoryBreakdown> getByCategory() { return byCategory; }
    public void setByCategory(List<CategoryBreakdown> byCategory) { this.byCategory = byCategory; }
    
    public Expense getTopExpense() { return topExpense; }
    public void setTopExpense(Expense topExpense) { this.topExpense = topExpense; }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private String month;
        private Double totalSpent;
        private List<CategoryBreakdown> byCategory;
        private Expense topExpense;
        
        public Builder month(String month) { this.month = month; return this; }
        public Builder totalSpent(Double totalSpent) { this.totalSpent = totalSpent; return this; }
        public Builder byCategory(List<CategoryBreakdown> byCategory) { this.byCategory = byCategory; return this; }
        public Builder topExpense(Expense topExpense) { this.topExpense = topExpense; return this; }
        
        public MonthlyAnalytics build() {
            return new MonthlyAnalytics(month, totalSpent, byCategory, topExpense);
        }
    }
}
