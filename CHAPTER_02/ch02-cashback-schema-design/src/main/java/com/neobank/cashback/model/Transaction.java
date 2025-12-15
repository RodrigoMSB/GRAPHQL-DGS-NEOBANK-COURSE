package com.neobank.cashback.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Transacción que genera cashback.
 * 
 * FLUJO:
 * 1. Usuario hace compra → createTransaction mutation
 * 2. Transaction creada con status PENDING
 * 3. Comercio confirma → status = CONFIRMED
 * 4. Se calcula cashback según tier + category
 * 5. Se crea Reward asociada
 * 
 * SECCIÓN 2.3: Queries y Mutations complejas
 * - Campo cashbackAmount es calculado (no persistido)
 * - Relación bidireccional con User y Reward
 */
public class Transaction {
    private String id;
    private String userId;              // FK a User
    private Double amount;              // Monto de la compra
    private TransactionCategory category;
    private String merchantName;        // Nombre del comercio
    private String description;
    private LocalDateTime transactionDate;
    private TransactionStatus status;
    
    // Campos calculados (se resuelven en resolver GraphQL)
    // cashbackAmount: amount * (tier.percentage * category.multiplier)
    // cashbackPercentage: tier.percentage * category.multiplier
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public Transaction() {
    }
    
    public Transaction(String id, String userId, Double amount, TransactionCategory category,
                       String merchantName, String description, LocalDateTime transactionDate,
                       TransactionStatus status) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.merchantName = merchantName;
        this.description = description;
        this.transactionDate = transactionDate;
        this.status = status;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public String getId() {
        return id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public TransactionCategory getCategory() {
        return category;
    }
    
    public String getMerchantName() {
        return merchantName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public LocalDateTime getTransactionDate() {
        return transactionDate;
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
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public void setCategory(TransactionCategory category) {
        this.category = category;
    }
    
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
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
                ", userId='" + userId + '\'' +
                ", amount=" + amount +
                ", category=" + category +
                ", merchantName='" + merchantName + '\'' +
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
        private String userId;
        private Double amount;
        private TransactionCategory category;
        private String merchantName;
        private String description;
        private LocalDateTime transactionDate;
        private TransactionStatus status;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder category(TransactionCategory category) {
            this.category = category;
            return this;
        }
        
        public Builder merchantName(String merchantName) {
            this.merchantName = merchantName;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder transactionDate(LocalDateTime transactionDate) {
            this.transactionDate = transactionDate;
            return this;
        }
        
        public Builder status(TransactionStatus status) {
            this.status = status;
            return this;
        }
        
        public Transaction build() {
            return new Transaction(id, userId, amount, category, merchantName,
                                  description, transactionDate, status);
        }
    }
}
