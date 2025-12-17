package com.neobank.cashback.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Recompensa de cashback ganada por una transacción.
 * 
 * CICLO DE VIDA:
 * 1. PENDING: Creada cuando Transaction pasa a CONFIRMED (30 días espera)
 * 2. AVAILABLE: Después de 30 días, lista para canjear
 * 3. REDEEMED: Usuario canjeó el cashback
 * 4. EXPIRED: 12 meses sin canjear (se pierde)
 * 
 * SECCIÓN 2.1: Diseño orientado a dominio
 * - Una Reward pertenece a un User y una Transaction
 * - Relationships: User (1) → Rewards (N), Transaction (1) → Reward (1)
 */
public class Reward {
    private String id;
    private String userId;              // FK a User
    private String transactionId;       // FK a Transaction
    private Double amount;              // Monto del cashback
    private LocalDateTime earnedAt;     // Cuándo se ganó
    private LocalDateTime expiresAt;    // 12 meses después de earnedAt
    private RewardStatus status;
    
    // Si fue canjeada
    private LocalDateTime redeemedAt;
    private String redemptionMethod;    // "BANK_TRANSFER", "GIFT_CARD", etc.
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public Reward() {
    }
    
    public Reward(String id, String userId, String transactionId, Double amount,
                  LocalDateTime earnedAt, LocalDateTime expiresAt, RewardStatus status,
                  LocalDateTime redeemedAt, String redemptionMethod) {
        this.id = id;
        this.userId = userId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.earnedAt = earnedAt;
        this.expiresAt = expiresAt;
        this.status = status;
        this.redeemedAt = redeemedAt;
        this.redemptionMethod = redemptionMethod;
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
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public RewardStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }
    
    public String getRedemptionMethod() {
        return redemptionMethod;
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
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public void setEarnedAt(LocalDateTime earnedAt) {
        this.earnedAt = earnedAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public void setStatus(RewardStatus status) {
        this.status = status;
    }
    
    public void setRedeemedAt(LocalDateTime redeemedAt) {
        this.redeemedAt = redeemedAt;
    }
    
    public void setRedemptionMethod(String redemptionMethod) {
        this.redemptionMethod = redemptionMethod;
    }
    
    // =========================================================================
    // EQUALS, HASHCODE, TOSTRING
    // =========================================================================
    
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
                ", transactionId='" + transactionId + '\'' +
                ", amount=" + amount +
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
        private String transactionId;
        private Double amount;
        private LocalDateTime earnedAt;
        private LocalDateTime expiresAt;
        private RewardStatus status;
        private LocalDateTime redeemedAt;
        private String redemptionMethod;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder earnedAt(LocalDateTime earnedAt) {
            this.earnedAt = earnedAt;
            return this;
        }
        
        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }
        
        public Builder status(RewardStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder redeemedAt(LocalDateTime redeemedAt) {
            this.redeemedAt = redeemedAt;
            return this;
        }
        
        public Builder redemptionMethod(String redemptionMethod) {
            this.redemptionMethod = redemptionMethod;
            return this;
        }
        
        public Reward build() {
            return new Reward(id, userId, transactionId, amount, earnedAt,
                            expiresAt, status, redeemedAt, redemptionMethod);
        }
    }
}
