package com.neobank.fraud.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class FraudAlert {
    
    private String id;
    private Transaction transaction;
    private RiskLevel riskLevel;
    private List<String> reasons;
    private LocalDateTime detectedAt;
    private String recommendedAction;
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public FraudAlert() {
    }
    
    public FraudAlert(String id, Transaction transaction, RiskLevel riskLevel,
                      List<String> reasons, LocalDateTime detectedAt, String recommendedAction) {
        this.id = id;
        this.transaction = transaction;
        this.riskLevel = riskLevel;
        this.reasons = reasons;
        this.detectedAt = detectedAt;
        this.recommendedAction = recommendedAction;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public String getId() {
        return id;
    }
    
    public Transaction getTransaction() {
        return transaction;
    }
    
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
    
    public List<String> getReasons() {
        return reasons;
    }
    
    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }
    
    public String getRecommendedAction() {
        return recommendedAction;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
    
    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }
    
    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }
    
    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }
    
    // =========================================================================
    // EQUALS, HASHCODE, TOSTRING
    // =========================================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FraudAlert that = (FraudAlert) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "FraudAlert{" +
                "id='" + id + '\'' +
                ", riskLevel=" + riskLevel +
                ", reasons=" + reasons +
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
        private Transaction transaction;
        private RiskLevel riskLevel;
        private List<String> reasons;
        private LocalDateTime detectedAt;
        private String recommendedAction;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder transaction(Transaction transaction) {
            this.transaction = transaction;
            return this;
        }
        
        public Builder riskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }
        
        public Builder reasons(List<String> reasons) {
            this.reasons = reasons;
            return this;
        }
        
        public Builder detectedAt(LocalDateTime detectedAt) {
            this.detectedAt = detectedAt;
            return this;
        }
        
        public Builder recommendedAction(String recommendedAction) {
            this.recommendedAction = recommendedAction;
            return this;
        }
        
        public FraudAlert build() {
            return new FraudAlert(id, transaction, riskLevel, reasons,
                                 detectedAt, recommendedAction);
        }
    }
}
