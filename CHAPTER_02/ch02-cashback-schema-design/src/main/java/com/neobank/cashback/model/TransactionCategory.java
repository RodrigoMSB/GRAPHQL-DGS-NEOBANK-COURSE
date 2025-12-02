package com.neobank.cashback.model;

/**
 * Categorías de transacciones para cashback diferenciado.
 * 
 * Cada categoría tiene un multiplicador que afecta el cashback final:
 * - TRAVEL: 3x (máximo beneficio)
 * - RESTAURANTS: 2x
 * - GROCERIES, GAS_STATIONS: 1.5x
 * - Otras categorías: 1x (cashback estándar)
 * 
 * EJEMPLO:
 * Usuario GOLD (3% base) compra en RESTAURANTS (2x):
 * Cashback = 3% × 2 = 6%
 */
public enum TransactionCategory {
    GROCERIES(1.5),           // Supermercados
    RESTAURANTS(2.0),         // Restaurantes (mayor cashback)
    GAS_STATIONS(1.5),        // Gasolineras
    TRAVEL(3.0),              // Vuelos, hoteles (máximo cashback)
    ENTERTAINMENT(1.0),       // Cine, streaming
    HEALTH_FITNESS(1.0),      // Gimnasio, salud
    SHOPPING(1.0),            // Retail general
    UTILITIES(1.0),           // Servicios básicos
    OTHER(1.0);               // Otros gastos
    
    private final double cashbackMultiplier;
    
    TransactionCategory(double multiplier) {
        this.cashbackMultiplier = multiplier;
    }
    
    /**
     * Obtiene el multiplicador de cashback para esta categoría.
     * 
     * @return Multiplicador (1.0 = sin bonus, 2.0 = doble cashback)
     */
    public double getCashbackMultiplier() {
        return cashbackMultiplier;
    }
}
