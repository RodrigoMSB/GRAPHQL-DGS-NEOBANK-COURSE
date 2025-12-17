package com.neobank.cashback.model.response;

import com.neobank.cashback.model.Transaction;

/**
 * Respuesta estructurada para mutations de Transaction.
 * 
 * 🎓 SECCIÓN 2.2: RESPONSE TYPES
 * 
 * ¿POR QUÉ UN RESPONSE TYPE?
 * 
 * En vez de retornar directamente Transaction (que podría ser null en error),
 * usamos un objeto estructurado que SIEMPRE tiene:
 * - success: Si la operación funcionó
 * - message: Explicación para el usuario
 * - transaction: Los datos (solo si success=true)
 * 
 * 💡 VENTAJAS:
 * 1. El cliente siempre recibe una respuesta consistente
 * 2. Los errores de negocio NO son excepciones GraphQL
 * 3. Fácil de manejar en el frontend
 * 4. El mismo patrón para todas las mutations
 * 
 * 💡 EJEMPLO EN GRAPHQL:
 * ```graphql
 * mutation {
 *   createTransaction(input: {...}) {
 *     success       # Siempre presente
 *     message       # Siempre presente
 *     transaction { # Presente solo si success=true
 *       id
 *       amount
 *     }
 *   }
 * }
 * ```
 * 
 * 💡 MANEJO EN FRONTEND:
 * ```javascript
 * const { success, message, transaction } = data.createTransaction;
 * if (success) {
 *   showSuccess(message);
 *   displayTransaction(transaction);
 * } else {
 *   showError(message);
 * }
 * ```
 */
public class TransactionResponse {
    
    /** ¿La operación fue exitosa? */
    private Boolean success;
    
    /** Mensaje descriptivo para el usuario */
    private String message;
    
    /** Transacción creada/modificada (null si error) */
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