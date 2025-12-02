package com.neobank.cashback.model;

/**
 * Estado de una recompensa de cashback.
 */
public enum RewardStatus {
    /**
     * Cashback pendiente (período de espera).
     * Típicamente 30 días después de la transacción.
     */
    PENDING,
    
    /**
     * Disponible para redimir.
     * El usuario puede canjearlo.
     */
    AVAILABLE,
    
    /**
     * Ya canjeado.
     * Transferido o usado.
     */
    REDEEMED,
    
    /**
     * Expirado sin canjear.
     * Después de 12 meses de inactividad.
     */
    EXPIRED
}
