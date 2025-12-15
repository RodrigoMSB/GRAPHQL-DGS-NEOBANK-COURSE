package com.neobank.cashback.model.input;

public class RedeemCashbackInput {
    private String userId;
    private Double amount;
    private String redemptionMethod;
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public RedeemCashbackInput() {
    }
    
    public RedeemCashbackInput(String userId, Double amount, String redemptionMethod) {
        this.userId = userId;
        this.amount = amount;
        this.redemptionMethod = redemptionMethod;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public String getUserId() {
        return userId;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public String getRedemptionMethod() {
        return redemptionMethod;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public void setRedemptionMethod(String redemptionMethod) {
        this.redemptionMethod = redemptionMethod;
    }
    
    // =========================================================================
    // BUILDER
    // =========================================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String userId;
        private Double amount;
        private String redemptionMethod;
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder redemptionMethod(String redemptionMethod) {
            this.redemptionMethod = redemptionMethod;
            return this;
        }
        
        public RedeemCashbackInput build() {
            return new RedeemCashbackInput(userId, amount, redemptionMethod);
        }
    }
}
