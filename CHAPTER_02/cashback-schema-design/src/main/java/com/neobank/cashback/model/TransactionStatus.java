package com.neobank.cashback.model;

/**
 * Estado de una transacción en el sistema.
 */
public enum TransactionStatus {
    /**
     * Pendiente de confirmación del comercio.
     * No genera cashback aún.
     */
    PENDING,
    
    /**
     * Confirmada por el comercio.
     * Cashback calculado y reward creada.
     */
    CONFIRMED,
    
    /**
     * Cancelada antes de confirmarse.
     * No genera cashback.
     */
    CANCELLED,
    
    /**
     * Reembolsada después de confirmación.
     * Cashback revertido si ya fue acreditado.
     */
    REFUNDED
}
