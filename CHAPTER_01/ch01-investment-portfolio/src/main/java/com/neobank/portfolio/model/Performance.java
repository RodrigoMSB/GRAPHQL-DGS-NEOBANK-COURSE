package com.neobank.portfolio.model;

import java.util.Objects;

/**
 * Modelo de dominio: Rendimiento del portfolio
 * 
 * Representa el rendimiento histórico de un portfolio de inversiones.
 * 
 * Sección 1.3: Se anida dentro de Portfolio para consultas complejas
 */
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
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public Performance() {
    }
    
    public Performance(Double totalReturn, Double yearReturn, Double monthReturn,
                       Double weekReturn, Asset bestPerformer, Asset worstPerformer) {
        this.totalReturn = totalReturn;
        this.yearReturn = yearReturn;
        this.monthReturn = monthReturn;
        this.weekReturn = weekReturn;
        this.bestPerformer = bestPerformer;
        this.worstPerformer = worstPerformer;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public Double getTotalReturn() {
        return totalReturn;
    }
    
    public Double getYearReturn() {
        return yearReturn;
    }
    
    public Double getMonthReturn() {
        return monthReturn;
    }
    
    public Double getWeekReturn() {
        return weekReturn;
    }
    
    public Asset getBestPerformer() {
        return bestPerformer;
    }
    
    public Asset getWorstPerformer() {
        return worstPerformer;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setTotalReturn(Double totalReturn) {
        this.totalReturn = totalReturn;
    }
    
    public void setYearReturn(Double yearReturn) {
        this.yearReturn = yearReturn;
    }
    
    public void setMonthReturn(Double monthReturn) {
        this.monthReturn = monthReturn;
    }
    
    public void setWeekReturn(Double weekReturn) {
        this.weekReturn = weekReturn;
    }
    
    public void setBestPerformer(Asset bestPerformer) {
        this.bestPerformer = bestPerformer;
    }
    
    public void setWorstPerformer(Asset worstPerformer) {
        this.worstPerformer = worstPerformer;
    }
    
    // =========================================================================
    // EQUALS, HASHCODE, TOSTRING
    // =========================================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Performance that = (Performance) o;
        return Objects.equals(totalReturn, that.totalReturn) &&
               Objects.equals(yearReturn, that.yearReturn) &&
               Objects.equals(monthReturn, that.monthReturn) &&
               Objects.equals(weekReturn, that.weekReturn);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalReturn, yearReturn, monthReturn, weekReturn);
    }
    
    @Override
    public String toString() {
        return "Performance{" +
                "totalReturn=" + totalReturn +
                ", yearReturn=" + yearReturn +
                ", monthReturn=" + monthReturn +
                ", weekReturn=" + weekReturn +
                ", bestPerformer=" + (bestPerformer != null ? bestPerformer.getSymbol() : "null") +
                ", worstPerformer=" + (worstPerformer != null ? worstPerformer.getSymbol() : "null") +
                '}';
    }
    
    // =========================================================================
    // BUILDER
    // =========================================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Double totalReturn;
        private Double yearReturn;
        private Double monthReturn;
        private Double weekReturn;
        private Asset bestPerformer;
        private Asset worstPerformer;
        
        public Builder totalReturn(Double totalReturn) {
            this.totalReturn = totalReturn;
            return this;
        }
        
        public Builder yearReturn(Double yearReturn) {
            this.yearReturn = yearReturn;
            return this;
        }
        
        public Builder monthReturn(Double monthReturn) {
            this.monthReturn = monthReturn;
            return this;
        }
        
        public Builder weekReturn(Double weekReturn) {
            this.weekReturn = weekReturn;
            return this;
        }
        
        public Builder bestPerformer(Asset bestPerformer) {
            this.bestPerformer = bestPerformer;
            return this;
        }
        
        public Builder worstPerformer(Asset worstPerformer) {
            this.worstPerformer = worstPerformer;
            return this;
        }
        
        public Performance build() {
            return new Performance(totalReturn, yearReturn, monthReturn,
                                  weekReturn, bestPerformer, worstPerformer);
        }
    }
}
