package com.neobank.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio: Rendimiento del portfolio
 * 
 * Representa el rendimiento histórico de un portfolio de inversiones.
 * 
 * Sección 1.3: Se anida dentro de Portfolio para consultas complejas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Performance {
    
    /**
     * Rendimiento total desde la creación del portfolio (%)
     */
    private Double totalReturn;
    
    /**
     * Rendimiento en el último año (%)
     */
    private Double yearReturn;
    
    /**
     * Rendimiento en el último mes (%)
     */
    private Double monthReturn;
    
    /**
     * Rendimiento en la última semana (%)
     */
    private Double weekReturn;
    
    /**
     * Mejor activo del portfolio (mayor ganancia %)
     */
    private Asset bestPerformer;
    
    /**
     * Peor activo del portfolio (menor ganancia o mayor pérdida %)
     */
    private Asset worstPerformer;
}
