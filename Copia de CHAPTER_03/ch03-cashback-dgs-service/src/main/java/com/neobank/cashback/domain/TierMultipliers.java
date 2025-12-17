package com.neobank.cashback.domain;

/**
 * Multiplicadores de cashback por tier de usuario.
 * 
 * Estos multiplicadores se aplican sobre el porcentaje base de cada categoría.
 */
public class TierMultipliers {
    
    private Double bronze;
    private Double silver;
    private Double gold;
    private Double platinum;
    
    public TierMultipliers() {
    }
    
    public TierMultipliers(Double bronze, Double silver, Double gold, Double platinum) {
        this.bronze = bronze;
        this.silver = silver;
        this.gold = gold;
        this.platinum = platinum;
    }
    
    public Double getBronze() {
        return bronze;
    }
    
    public void setBronze(Double bronze) {
        this.bronze = bronze;
    }
    
    public Double getSilver() {
        return silver;
    }
    
    public void setSilver(Double silver) {
        this.silver = silver;
    }
    
    public Double getGold() {
        return gold;
    }
    
    public void setGold(Double gold) {
        this.gold = gold;
    }
    
    public Double getPlatinum() {
        return platinum;
    }
    
    public void setPlatinum(Double platinum) {
        this.platinum = platinum;
    }
}
