package com.neobank.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private List<Asset> assets = new ArrayList<>();
    
    /**
     * Rendimiento histórico del portfolio
     * Sección 1.3: Otra relación anidada
     */
    private Performance performance;
    
    /**
     * Calcula el valor total del portfolio sumando todos los activos
     */
    public void calculateTotalValue() {
        this.totalValue = assets.stream()
                .mapToDouble(Asset::getTotalValue)
                .sum();
    }
}
