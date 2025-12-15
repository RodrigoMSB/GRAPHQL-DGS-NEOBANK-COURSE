package com.neobank.analytics.model;

import java.time.LocalDate;
import java.util.Objects;

public class Expense {
    
    private String id;
    private String accountId;
    private Double amount;
    private String currency;
    private String merchantName;
    private Category category;
    private LocalDate date;
    private String description;
    
    public Expense() {
    }
    
    public Expense(String id, String accountId, Double amount, String currency,
                   String merchantName, Category category, LocalDate date, String description) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.merchantName = merchantName;
        this.category = category;
        this.date = date;
        this.description = description;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return Objects.equals(id, expense.id);
    }
    
    @Override
    public int hashCode() { return Objects.hash(id); }
    
    @Override
    public String toString() {
        return "Expense{id='" + id + "', amount=" + amount + ", merchantName='" + merchantName + "'}";
    }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private String id;
        private String accountId;
        private Double amount;
        private String currency;
        private String merchantName;
        private Category category;
        private LocalDate date;
        private String description;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder accountId(String accountId) { this.accountId = accountId; return this; }
        public Builder amount(Double amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder merchantName(String merchantName) { this.merchantName = merchantName; return this; }
        public Builder category(Category category) { this.category = category; return this; }
        public Builder date(LocalDate date) { this.date = date; return this; }
        public Builder description(String description) { this.description = description; return this; }
        
        public Expense build() {
            return new Expense(id, accountId, amount, currency, merchantName, category, date, description);
        }
    }
}
