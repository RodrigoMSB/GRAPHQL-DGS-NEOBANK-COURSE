package com.neobank.cashback.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Regla de cashback por categoría.
 * 
 * Define el porcentaje base de cashback y los multiplicadores por tier.
 * Ejemplo: GROCERIES tiene 2% base, con multiplicadores por tier.
 */
public class CashbackRule {
    
    private String id;
    private TransactionCategory category;
    private Double basePercentage;
    private TierMultipliers tierMultipliers;
    private BigDecimal minTransactionAmount;
    private BigDecimal maxCashbackPerTransaction;
    private Boolean isActive;
    
    public CashbackRule() {
    }
    
    public CashbackRule(String id, TransactionCategory category, Double basePercentage,
                        TierMultipliers tierMultipliers, BigDecimal minTransactionAmount,
                        BigDecimal maxCashbackPerTransaction, Boolean isActive) {
        this.id = id;
        this.category = category;
        this.basePercentage = basePercentage;
        this.tierMultipliers = tierMultipliers;
        this.minTransactionAmount = minTransactionAmount;
        this.maxCashbackPerTransaction = maxCashbackPerTransaction;
        this.isActive = isActive;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public TransactionCategory getCategory() {
        return category;
    }
    
    public void setCategory(TransactionCategory category) {
        this.category = category;
    }
    
    public Double getBasePercentage() {
        return basePercentage;
    }
    
    public void setBasePercentage(Double basePercentage) {
        this.basePercentage = basePercentage;
    }
    
    public TierMultipliers getTierMultipliers() {
        return tierMultipliers;
    }
    
    public void setTierMultipliers(TierMultipliers tierMultipliers) {
        this.tierMultipliers = tierMultipliers;
    }
    
    public BigDecimal getMinTransactionAmount() {
        return minTransactionAmount;
    }
    
    public void setMinTransactionAmount(BigDecimal minTransactionAmount) {
        this.minTransactionAmount = minTransactionAmount;
    }
    
    public BigDecimal getMaxCashbackPerTransaction() {
        return maxCashbackPerTransaction;
    }
    
    public void setMaxCashbackPerTransaction(BigDecimal maxCashbackPerTransaction) {
        this.maxCashbackPerTransaction = maxCashbackPerTransaction;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CashbackRule that = (CashbackRule) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
