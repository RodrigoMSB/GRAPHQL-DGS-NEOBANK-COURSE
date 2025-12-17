package com.neobank.cashback.model.input;

/**
 * Input para canjear cashback acumulado.
 * 
 * 🎓 SECCIÓN 2.2: INPUT TYPES
 * 
 * Este input permite al usuario canjear su cashback disponible
 * mediante diferentes métodos de redención.
 * 
 * 📦 CAMPOS:
 * - userId: Usuario que canjea
 * - amount: Monto a canjear (debe ser <= availableCashback)
 * - redemptionMethod: Cómo recibir el dinero
 * 
 * 💡 MÉTODOS DE REDENCIÓN SOPORTADOS:
 * - "BANK_TRANSFER": Transferencia a cuenta bancaria
 * - "GIFT_CARD": Tarjeta de regalo (Amazon, etc.)
 * - "STATEMENT_CREDIT": Crédito en estado de cuenta
 * - "CHARITY": Donación a caridad
 * 
 * 💡 EJEMPLO DE USO:
 * ```graphql
 * mutation {
 *   redeemCashback(input: {
 *     userId: "user-001"
 *     amount: 50.00
 *     redemptionMethod: "BANK_TRANSFER"
 *   }) {
 *     success
 *     message
 *   }
 * }
 * ```
 */
public class RedeemCashbackInput {
    
    /** ID del usuario que canjea el cashback */
    private String userId;
    
    /** Monto a canjear (debe ser <= availableCashback) */
    private Double amount;
    
    /** Método de redención (BANK_TRANSFER, GIFT_CARD, etc.) */
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