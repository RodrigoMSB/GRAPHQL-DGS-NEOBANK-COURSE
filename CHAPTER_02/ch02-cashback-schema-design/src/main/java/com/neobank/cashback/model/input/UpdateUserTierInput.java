package com.neobank.cashback.model.input;

import com.neobank.cashback.model.CashbackTier;

public class UpdateUserTierInput {
    private String userId;
    private CashbackTier newTier;
    private String reason;
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public UpdateUserTierInput() {
    }
    
    public UpdateUserTierInput(String userId, CashbackTier newTier, String reason) {
        this.userId = userId;
        this.newTier = newTier;
        this.reason = reason;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public String getUserId() {
        return userId;
    }
    
    public CashbackTier getNewTier() {
        return newTier;
    }
    
    public String getReason() {
        return reason;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public void setNewTier(CashbackTier newTier) {
        this.newTier = newTier;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    // =========================================================================
    // BUILDER
    // =========================================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String userId;
        private CashbackTier newTier;
        private String reason;
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder newTier(CashbackTier newTier) {
            this.newTier = newTier;
            return this;
        }
        
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }
        
        public UpdateUserTierInput build() {
            return new UpdateUserTierInput(userId, newTier, reason);
        }
    }
}
