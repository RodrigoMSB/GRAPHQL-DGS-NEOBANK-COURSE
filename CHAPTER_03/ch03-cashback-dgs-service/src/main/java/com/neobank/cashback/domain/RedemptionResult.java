package com.neobank.cashback.domain;

import java.math.BigDecimal;

/**
 * Resultado de una operación de redención de cashback.
 * 
 * Se usa como respuesta de la mutation redeemCashback.
 */
public class RedemptionResult {
    
    private Boolean success;
    private String message;
    private BigDecimal redeemedAmount;
    private BigDecimal newBalance;
    private String transactionId;
    
    public RedemptionResult() {
    }
    
    public RedemptionResult(Boolean success, String message, BigDecimal redeemedAmount,
                           BigDecimal newBalance, String transactionId) {
        this.success = success;
        this.message = message;
        this.redeemedAmount = redeemedAmount;
        this.newBalance = newBalance;
        this.transactionId = transactionId;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public BigDecimal getRedeemedAmount() {
        return redeemedAmount;
    }
    
    public void setRedeemedAmount(BigDecimal redeemedAmount) {
        this.redeemedAmount = redeemedAmount;
    }
    
    public BigDecimal getNewBalance() {
        return newBalance;
    }
    
    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
