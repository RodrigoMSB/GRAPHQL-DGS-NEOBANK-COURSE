package com.neobank.portfolio.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Modelo de dominio: Portfolio de inversión
 * 
 * Representa un portfolio de inversiones de un cliente en el NeoBank.
 * Este modelo se usa tanto en REST como en GraphQL.
 * 
 * Sección 1.2: Define la estructura de datos principal
 * Sección 1.3: Permite relaciones anidadas (assets, performance)
 * Sección 1.5: Campos non-nullable en el schema GraphQL
 */
public class Portfolio {
    
    /**
     * ID único del portfolio
     */
    private String id;
    
    /**
     * Nombre del portfolio (ej: "Retirement Fund", "Growth Portfolio")
     */
    private String name;
    
    /**
     * ID del propietario del portfolio
     */
    private String ownerId;
    
    /**
     * Nombre del propietario
     */
    private String ownerName;
    
    /**
     * Valor total actual del portfolio en USD
     */
    private Double totalValue;
    
    /**
     * Fecha de creación del portfolio
     */
    private LocalDateTime createdAt;
    
    /**
     * Lista de activos en el portfolio
     * Sección 1.3: Permite consultas anidadas como portfolio { assets { symbol } }
     */
    private List<Asset> assets = new ArrayList<>();
    
    /**
     * Rendimiento histórico del portfolio
     * Sección 1.3: Otra relación anidada
     */
    private Performance performance;
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public Portfolio() {
        this.assets = new ArrayList<>();
    }
    
    public Portfolio(String id, String name, String ownerId, String ownerName,
                     Double totalValue, LocalDateTime createdAt, List<Asset> assets,
                     Performance performance) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.totalValue = totalValue;
        this.createdAt = createdAt;
        this.assets = assets != null ? assets : new ArrayList<>();
        this.performance = performance;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getOwnerId() {
        return ownerId;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public Double getTotalValue() {
        return totalValue;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public List<Asset> getAssets() {
        return assets;
    }
    
    public Performance getPerformance() {
        return performance;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setAssets(List<Asset> assets) {
        this.assets = assets != null ? assets : new ArrayList<>();
    }
    
    public void setPerformance(Performance performance) {
        this.performance = performance;
    }
    
    // =========================================================================
    // BUSINESS METHODS
    // =========================================================================
    
    /**
     * Calcula el valor total del portfolio sumando todos los activos
     */
    public void calculateTotalValue() {
        this.totalValue = assets.stream()
                .mapToDouble(Asset::getTotalValue)
                .sum();
    }
    
    // =========================================================================
    // EQUALS, HASHCODE, TOSTRING
    // =========================================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Portfolio portfolio = (Portfolio) o;
        return Objects.equals(id, portfolio.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Portfolio{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", totalValue=" + totalValue +
                ", createdAt=" + createdAt +
                ", assetsCount=" + (assets != null ? assets.size() : 0) +
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
        private String name;
        private String ownerId;
        private String ownerName;
        private Double totalValue;
        private LocalDateTime createdAt;
        private List<Asset> assets = new ArrayList<>();
        private Performance performance;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }
        
        public Builder ownerName(String ownerName) {
            this.ownerName = ownerName;
            return this;
        }
        
        public Builder totalValue(Double totalValue) {
            this.totalValue = totalValue;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder assets(List<Asset> assets) {
            this.assets = assets != null ? assets : new ArrayList<>();
            return this;
        }
        
        public Builder performance(Performance performance) {
            this.performance = performance;
            return this;
        }
        
        public Portfolio build() {
            return new Portfolio(id, name, ownerId, ownerName, totalValue,
                               createdAt, assets, performance);
        }
    }
}
