package com.neobank.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Modelo de dominio: Activo financiero
 * 
 * Representa un activo (stock, crypto, ETF, etc.) dentro de un portfolio.
 * 
 * Sección 1.2: Define tipos de datos estructurados
 * Sección 1.4: Se puede filtrar, ordenar y paginar
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {
    
    /**
     * ID único del activo
     */
    private String id;
    
    /**
     * Símbolo del activo (ej: AAPL, GOOGL, BTC)
     */
    private String symbol;
    
    /**
     * Nombre completo del activo
     */
    private String name;
    
    /**
     * Tipo de activo
     * Sección 1.2: Uso de Enums en GraphQL
     */
    private AssetType assetType;
    
    /**
     * Cantidad de unidades
     */
    private Double quantity;
    
    /**
     * Precio de compra promedio
     */
    private Double averageBuyPrice;
    
    /**
     * Precio actual del mercado
     */
    private Double currentPrice;
    
    /**
     * Fecha de última actualización de precio
     */
    private LocalDateTime lastUpdated;
    
    /**
     * Calcula el valor total del activo (quantity * currentPrice)
     */
    public Double getTotalValue() {
        return quantity * currentPrice;
    }
    
    /**
     * Calcula la ganancia/pérdida en porcentaje
     */
    public Double getProfitLossPercent() {
        if (averageBuyPrice == null || averageBuyPrice == 0) {
            return 0.0;
        }
        return ((currentPrice - averageBuyPrice) / averageBuyPrice) * 100;
    }
}
