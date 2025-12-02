package com.neobank.cashback.domain;

/**
 * Estados posibles de una recompensa cashback.
 * 
 * Ciclo de vida típico:
 * PENDING -> ACTIVE -> REDEEMED (happy path)
 *         -> ACTIVE -> EXPIRED (si no se usa a tiempo)
 *         -> CANCELLED (por políticas o fraude)
 */
public enum RewardStatus {
    /**
     * Reward recién creada, pendiente de validación
     */
    PENDING,
    
    /**
     * Reward activa y disponible para usar
     */
    ACTIVE,
    
    /**
     * Reward que excedió su fecha de expiración
     */
    EXPIRED,
    
    /**
     * Reward ya utilizada/canjeada por el usuario
     */
    REDEEMED,
    
    /**
     * Reward cancelada por políticas o detección de fraude
     */
    CANCELLED
}
