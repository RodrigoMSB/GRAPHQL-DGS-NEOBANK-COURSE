package com.neobank.portfolio.model;

/**
 * Tipos de activos financieros soportados
 * 
 * Sección 1.2: Demuestra el uso de Enums en GraphQL
 * Sección 1.5: Validación automática - solo acepta estos valores
 */
public enum AssetType {
    /**
     * Acciones de empresas
     */
    STOCK,
    
    /**
     * Criptomonedas
     */
    CRYPTO,
    
    /**
     * Fondos cotizados (Exchange-Traded Funds)
     */
    ETF,
    
    /**
     * Bonos
     */
    BOND,
    
    /**
     * Commodities (oro, plata, petróleo, etc.)
     */
    COMMODITY
}
