package com.neobank.cashback.domain;

/**
 * Niveles de membresía del programa de rewards.
 * 
 * Cada tier tiene multiplicadores diferentes:
 * - BRONZE: 1.0x (base)
 * - SILVER: 1.5x
 * - GOLD: 2.0x
 * - PLATINUM: 3.0x
 */
public enum RewardTier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM
}
