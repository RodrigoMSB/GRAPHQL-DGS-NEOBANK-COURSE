package com.neobank.cashback.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Recompensa cashback individual generada por una transacción.
 * 
 * Cada vez que un usuario hace una compra elegible, se genera una Reward.
 * La reward tiene un monto, categoría, fecha de expiración y estado.
 */
public class Reward {
    
    private String id;
    private String userId;
    private BigDecimal amount;
    private LocalDateTime earnedAt;
    private LocalDateTime expiresAt;
    private RewardStatus status;
    private TransactionCategory category;
    private String transactionId;
    private String description;
    private Double multiplier;
    
    // Constructor vacío
    public Reward() {
    }
    
    // Constructor completo
    public Reward(String id, String userId, BigDecimal amount, 
                  LocalDateTime earnedAt, LocalDateTime expiresAt, 
                  RewardStatus status, TransactionCategory category, 
                  String transactionId, String description, Double multiplier) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.earnedAt = earnedAt;
        this.expiresAt = expiresAt;
        this.status = status;
        this.category = category;
        this.transactionId = transactionId;
        this.description = description;
        this.multiplier = multiplier;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }
    
    public void setEarnedAt(LocalDateTime earnedAt) {
        this.earnedAt = earnedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public RewardStatus getStatus() {
        return status;
    }
    
    public void setStatus(RewardStatus status) {
        this.status = status;
    }
    
    public TransactionCategory getCategory() {
        return category;
    }
    
    public void setCategory(TransactionCategory category) {
        this.category = category;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getMultiplier() {
        return multiplier;
    }
    
    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reward reward = (Reward) o;
        return Objects.equals(id, reward.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Reward{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                ", category=" + category +
                '}';
    }
}
