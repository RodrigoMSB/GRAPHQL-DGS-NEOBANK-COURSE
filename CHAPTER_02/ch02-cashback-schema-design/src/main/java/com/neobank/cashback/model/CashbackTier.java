package com.neobank.cashback.model;

/**
 * Niveles del programa de cashback.
 * 
 * Cada tier otorga un porcentaje diferente de cashback:
 * - BRONZE: 1%
 * - SILVER: 2%
 * - GOLD: 3%
 * - PLATINUM: 5%
 * 
 * El tier puede cambiar basado en:
 * - Volumen de transacciones mensuales
 * - Antigüedad en el programa
 * - Promociones especiales
 */
public enum CashbackTier {
    BRONZE(1.0),
    SILVER(2.0),
    GOLD(3.0),
    PLATINUM(5.0);
    
    private final double basePercentage;
    
    CashbackTier(double basePercentage) {
        this.basePercentage = basePercentage;
    }
    
    /**
     * Obtiene el porcentaje base de cashback para este tier.
     * 
     * @return Porcentaje base (1.0 = 1%)
     */
    public double getBasePercentage() {
        return basePercentage;
    }
    
    /**
     * Calcula el porcentaje de cashback para una categoría específica.
     * 
     * Algunas categorías tienen multipliers:
     * - GROCERIES, GAS_STATIONS: 1.5x
     * - RESTAURANTS: 2x
     * - TRAVEL: 3x
     * - Otras: 1x
     * 
     * @param category Categoría de la transacción
     * @return Porcentaje final de cashback
     */
    public double getCashbackPercentage(TransactionCategory category) {
        double multiplier = category.getCashbackMultiplier();
        return basePercentage * multiplier;
    }
}
