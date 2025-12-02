package com.neobank.cashback.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Recompensa de cashback ganada por una transacción.
 * 
 * CICLO DE VIDA:
 * 1. PENDING: Creada cuando Transaction pasa a CONFIRMED (30 días espera)
 * 2. AVAILABLE: Después de 30 días, lista para canjear
 * 3. REDEEMED: Usuario canjeó el cashback
 * 4. EXPIRED: 12 meses sin canjear (se pierde)
 * 
 * SECCIÓN 2.1: Diseño orientado a dominio
 * - Una Reward pertenece a un User y una Transaction
 * - Relationships: User (1) → Rewards (N), Transaction (1) → Reward (1)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reward {
    private String id;
    private String userId;              // FK a User
    private String transactionId;       // FK a Transaction
    private Double amount;              // Monto del cashback
    private LocalDateTime earnedAt;     // Cuándo se ganó
    private LocalDateTime expiresAt;    // 12 meses después de earnedAt
    private RewardStatus status;
    
    // Si fue canjeada
    private LocalDateTime redeemedAt;
    private String redemptionMethod;    // "BANK_TRANSFER", "GIFT_CARD", etc.
}
