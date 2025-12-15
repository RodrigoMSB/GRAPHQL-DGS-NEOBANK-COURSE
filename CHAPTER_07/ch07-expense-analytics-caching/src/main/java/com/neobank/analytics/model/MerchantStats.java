package com.neobank.analytics.model;

public class MerchantStats {
    
    private String merchantName;
    private Double totalSpent;
    private Integer transactionCount;
    
    public MerchantStats() {
    }
    
    public MerchantStats(String merchantName, Double totalSpent, Integer transactionCount) {
        this.merchantName = merchantName;
        this.totalSpent = totalSpent;
        this.transactionCount = transactionCount;
    }
    
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    
    public Double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }
    
    public Integer getTransactionCount() { return transactionCount; }
    public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private String merchantName;
        private Double totalSpent;
        private Integer transactionCount;
        
        public Builder merchantName(String merchantName) { this.merchantName = merchantName; return this; }
        public Builder totalSpent(Double totalSpent) { this.totalSpent = totalSpent; return this; }
        public Builder transactionCount(Integer transactionCount) { this.transactionCount = transactionCount; return this; }
        
        public MerchantStats build() {
            return new MerchantStats(merchantName, totalSpent, transactionCount);
        }
    }
}
