package com.neobank.portfolio.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modelo de dominio: Activo financiero
 * 
 * Representa un activo (stock, crypto, ETF, etc.) dentro de un portfolio.
 * 
 * Sección 1.2: Define tipos de datos estructurados
 * Sección 1.4: Se puede filtrar, ordenar y paginar
 */
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
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public Asset() {
    }
    
    public Asset(String id, String symbol, String name, AssetType assetType,
                 Double quantity, Double averageBuyPrice, Double currentPrice,
                 LocalDateTime lastUpdated) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.assetType = assetType;
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
        this.currentPrice = currentPrice;
        this.lastUpdated = lastUpdated;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public String getId() {
        return id;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getName() {
        return name;
    }
    
    public AssetType getAssetType() {
        return assetType;
    }
    
    public Double getQuantity() {
        return quantity;
    }
    
    public Double getAverageBuyPrice() {
        return averageBuyPrice;
    }
    
    public Double getCurrentPrice() {
        return currentPrice;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }
    
    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
    
    public void setAverageBuyPrice(Double averageBuyPrice) {
        this.averageBuyPrice = averageBuyPrice;
    }
    
    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    // =========================================================================
    // BUSINESS METHODS
    // =========================================================================
    
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
    
    // =========================================================================
    // EQUALS, HASHCODE, TOSTRING
    // =========================================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return Objects.equals(id, asset.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Asset{" +
                "id='" + id + '\'' +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", assetType=" + assetType +
                ", quantity=" + quantity +
                ", averageBuyPrice=" + averageBuyPrice +
                ", currentPrice=" + currentPrice +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
    
    // =========================================================================
    // BUILDER
    // =========================================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String symbol;
        private String name;
        private AssetType assetType;
        private Double quantity;
        private Double averageBuyPrice;
        private Double currentPrice;
        private LocalDateTime lastUpdated;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder assetType(AssetType assetType) {
            this.assetType = assetType;
            return this;
        }
        
        public Builder quantity(Double quantity) {
            this.quantity = quantity;
            return this;
        }
        
        public Builder averageBuyPrice(Double averageBuyPrice) {
            this.averageBuyPrice = averageBuyPrice;
            return this;
        }
        
        public Builder currentPrice(Double currentPrice) {
            this.currentPrice = currentPrice;
            return this;
        }
        
        public Builder lastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }
        
        public Asset build() {
            return new Asset(id, symbol, name, assetType, quantity,
                           averageBuyPrice, currentPrice, lastUpdated);
        }
    }
}
