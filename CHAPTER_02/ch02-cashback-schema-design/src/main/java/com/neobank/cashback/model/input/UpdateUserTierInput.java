package com.neobank.cashback.model.input;

import com.neobank.cashback.model.CashbackTier;

/**
 * Input para actualizar el tier de un usuario.
 * 
 * 🎓 SECCIÓN 2.2: INPUT TYPES
 * 
 * Este input permite a los administradores cambiar el nivel
 * de un usuario en el programa de cashback.
 * 
 * 📦 CAMPOS:
 * - userId: Usuario a actualizar
 * - newTier: Nuevo nivel (BRONZE, SILVER, GOLD, PLATINUM)
 * - reason: Motivo del cambio (para auditoría)
 * 
 * 💡 RAZONES COMUNES:
 * - "PROMOTION": Promoción por volumen de compras
 * - "SPECIAL_OFFER": Oferta especial temporal
 * - "LOYALTY_REWARD": Recompensa por antigüedad
 * - "ADJUSTMENT": Ajuste administrativo
 * 
 * 💡 EJEMPLO DE USO:
 * ```graphql
 * mutation {
 *   updateUserTier(input: {
 *     userId: "user-001"
 *     newTier: PLATINUM
 *     reason: "Promoción por alto volumen de compras"
 *   }) {
 *     success
 *     message
 *     user {
 *       tier
 *     }
 *   }
 * }
 * ```
 */
public class UpdateUserTierInput {
    
    /** ID del usuario a actualizar */
    private String userId;
    
    /** Nuevo tier a asignar */
    private CashbackTier newTier;
    
    /** Razón del cambio (para auditoría) */
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