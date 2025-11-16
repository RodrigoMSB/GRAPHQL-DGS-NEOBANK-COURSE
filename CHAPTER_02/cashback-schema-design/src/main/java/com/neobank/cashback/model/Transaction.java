package com.neobank.cashback.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Transacción que genera cashback.
 * 
 * FLUJO:
 * 1. Usuario hace compra → createTransaction mutation
 * 2. Transaction creada con status PENDING
 * 3. Comercio confirma → status = CONFIRMED
 * 4. Se calcula cashback según tier + category
 * 5. Se crea Reward asociada
 * 
 * SECCIÓN 2.3: Queries y Mutations complejas
 * - Campo cashbackAmount es calculado (no persistido)
 * - Relación bidireccional con User y Reward
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private String id;
    private String userId;              // FK a User
    private Double amount;              // Monto de la compra
    private TransactionCategory category;
    private String merchantName;        // Nombre del comercio
    private String description;
    private LocalDateTime transactionDate;
    private TransactionStatus status;
    
    // Campos calculados (se resuelven en resolver GraphQL)
    // cashbackAmount: amount * (tier.percentage * category.multiplier)
    // cashbackPercentage: tier.percentage * category.multiplier
}
