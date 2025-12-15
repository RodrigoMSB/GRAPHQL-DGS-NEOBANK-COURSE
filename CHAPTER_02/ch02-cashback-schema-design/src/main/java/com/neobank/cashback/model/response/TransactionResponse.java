package com.neobank.cashback.model.response;

import com.neobank.cashback.model.Transaction;

public class TransactionResponse {
    private Boolean success;
    private String message;
    private Transaction transaction;
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public TransactionResponse() {
    }
    
    public TransactionResponse(Boolean success, String message, Transaction transaction) {
        this.success = success;
        this.message = message;
        this.transaction = transaction;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public Boolean getSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Transaction getTransaction() {
        return transaction;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
    
    // =========================================================================
    // BUILDER
    // =========================================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Boolean success;
        private String message;
        private Transaction transaction;
        
        public Builder success(Boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder transaction(Transaction transaction) {
            this.transaction = transaction;
            return this;
        }
        
        public TransactionResponse build() {
            return new TransactionResponse(success, message, transaction);
        }
    }
}
