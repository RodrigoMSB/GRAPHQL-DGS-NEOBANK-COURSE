package com.neobank.cashback.model.input;

import com.neobank.cashback.model.TransactionCategory;

import java.time.LocalDateTime;

/**
 * Input para crear una nueva transacción.
 * 
 * SECCIÓN 2.2: Diferencia entre INPUT TYPES y OUTPUT TYPES
 * 
 * ¿Por qué un Input separado y no reusar Transaction?
 * 
 * 1. FLEXIBILIDAD:
 *    - Input puede tener campos diferentes al output
 *    - Ej: Input no tiene 'id' (se genera en servidor)
 *    - Ej: Input no tiene 'status' (siempre empieza PENDING)
 * 
 * 2. VALIDACIÓN:
 *    - Inputs pueden tener validaciones específicas
 *    - Required fields diferentes entre create/update
 * 
 * 3. EVOLUCIÓN:
 *    - Puedes cambiar Input sin afectar Output (y viceversa)
 *    - Backward compatibility más fácil
 * 
 * REGLA GraphQL:
 * Input types NO pueden tener campos de tipo Object, solo escalares/enums/otros inputs
 */
public class CreateTransactionInput {
    private String userId;
    private Double amount;
    private TransactionCategory category;
    private String merchantName;
    private String description;         // Opcional
    private LocalDateTime transactionDate;  // Opcional (default: now)
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public CreateTransactionInput() {
    }
    
    public CreateTransactionInput(String userId, Double amount, TransactionCategory category,
                                  String merchantName, String description,
                                  LocalDateTime transactionDate) {
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.merchantName = merchantName;
        this.description = description;
        this.transactionDate = transactionDate;
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
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
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
    
    // =========================================================================
    // BUILDER
    // =========================================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String userId;
        private Double amount;
        private TransactionCategory category;
        private String merchantName;
        private String description;
        private LocalDateTime transactionDate;
        
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
        
        public CreateTransactionInput build() {
            return new CreateTransactionInput(userId, amount, category,
                                             merchantName, description, transactionDate);
        }
    }
}
