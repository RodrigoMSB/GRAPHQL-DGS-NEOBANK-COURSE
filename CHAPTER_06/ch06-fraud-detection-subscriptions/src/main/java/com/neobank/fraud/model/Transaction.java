package com.neobank.fraud.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction {
    
    private String id;
    private String accountId;
    private Double amount;
    private String currency;
    private String merchantName;
    private String category;
    private String location;
    private LocalDateTime timestamp;
    private Double riskScore;
    private TransactionStatus status;
    
    public enum TransactionStatus {
        PENDING,
        APPROVED,
        REJECTED,
        FLAGGED
    }
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public Transaction() {
    }
    
    public Transaction(String id, String accountId, Double amount, String currency,
                       String merchantName, String category, String location,
                       LocalDateTime timestamp, Double riskScore, TransactionStatus status) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.merchantName = merchantName;
        this.category = category;
        this.location = location;
        this.timestamp = timestamp;
        this.riskScore = riskScore;
        this.status = status;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public String getId() {
        return id;
    }
    
    public String getAccountId() {
        return accountId;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public String getMerchantName() {
        return merchantName;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getLocation() {
        return location;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public Double getRiskScore() {
        return riskScore;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    // =========================================================================
    // EQUALS, HASHCODE, TOSTRING
    // =========================================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", accountId='" + accountId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                '}';
    }
    
    // =========================================================================
    // BUILDER
    // =========================================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String accountId;
        private Double amount;
        private String currency;
        private String merchantName;
        private String category;
        private String location;
        private LocalDateTime timestamp;
        private Double riskScore;
        private TransactionStatus status;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }
        
        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }
        
        public Builder merchantName(String merchantName) {
            this.merchantName = merchantName;
            return this;
        }
        
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        
        public Builder location(String location) {
            this.location = location;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder riskScore(Double riskScore) {
            this.riskScore = riskScore;
            return this;
        }
        
        public Builder status(TransactionStatus status) {
            this.status = status;
            return this;
        }
        
        public Transaction build() {
            return new Transaction(id, accountId, amount, currency, merchantName,
                                  category, location, timestamp, riskScore, status);
        }
    }
}
