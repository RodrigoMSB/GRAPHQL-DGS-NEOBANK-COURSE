package com.neobank.carbon.model;

import java.time.LocalDate;
import java.util.Objects;

public class Transaction {
    
    private String id;
    private String accountId;
    private Double amount;
    private String currency;
    private String merchantName;
    private MerchantCategory merchantCategory;
    private LocalDate date;
    private CarbonFootprint carbonFootprint;
    private ESGScore esgScore;
    
    @Deprecated
    private String category;
    
    @Deprecated
    private Boolean hasOffset;
    
    public Transaction() {}
    
    public Transaction(String id, String accountId, Double amount, String currency,
                       String merchantName, MerchantCategory merchantCategory, LocalDate date,
                       CarbonFootprint carbonFootprint, ESGScore esgScore,
                       String category, Boolean hasOffset) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.merchantName = merchantName;
        this.merchantCategory = merchantCategory;
        this.date = date;
        this.carbonFootprint = carbonFootprint;
        this.esgScore = esgScore;
        this.category = category;
        this.hasOffset = hasOffset;
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
    public MerchantCategory getMerchantCategory() { return merchantCategory; }
    public void setMerchantCategory(MerchantCategory merchantCategory) { this.merchantCategory = merchantCategory; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public CarbonFootprint getCarbonFootprint() { return carbonFootprint; }
    public void setCarbonFootprint(CarbonFootprint carbonFootprint) { this.carbonFootprint = carbonFootprint; }
    public ESGScore getEsgScore() { return esgScore; }
    public void setEsgScore(ESGScore esgScore) { this.esgScore = esgScore; }
    @Deprecated public String getCategory() { return category; }
    @Deprecated public void setCategory(String category) { this.category = category; }
    @Deprecated public Boolean getHasOffset() { return hasOffset; }
    @Deprecated public void setHasOffset(Boolean hasOffset) { this.hasOffset = hasOffset; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((Transaction) o).id);
    }
    
    @Override
    public int hashCode() { return Objects.hash(id); }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private String id, accountId, currency, merchantName, category;
        private Double amount;
        private MerchantCategory merchantCategory;
        private LocalDate date;
        private CarbonFootprint carbonFootprint;
        private ESGScore esgScore;
        private Boolean hasOffset;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder accountId(String accountId) { this.accountId = accountId; return this; }
        public Builder amount(Double amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder merchantName(String merchantName) { this.merchantName = merchantName; return this; }
        public Builder merchantCategory(MerchantCategory merchantCategory) { this.merchantCategory = merchantCategory; return this; }
        public Builder date(LocalDate date) { this.date = date; return this; }
        public Builder carbonFootprint(CarbonFootprint carbonFootprint) { this.carbonFootprint = carbonFootprint; return this; }
        public Builder esgScore(ESGScore esgScore) { this.esgScore = esgScore; return this; }
        @Deprecated public Builder category(String category) { this.category = category; return this; }
        @Deprecated public Builder hasOffset(Boolean hasOffset) { this.hasOffset = hasOffset; return this; }
        
        public Transaction build() {
            return new Transaction(id, accountId, amount, currency, merchantName, merchantCategory,
                                  date, carbonFootprint, esgScore, category, hasOffset);
        }
    }
}
