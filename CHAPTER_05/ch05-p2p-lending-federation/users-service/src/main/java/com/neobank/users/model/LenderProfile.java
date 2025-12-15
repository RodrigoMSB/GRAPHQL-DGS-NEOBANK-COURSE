package com.neobank.users.model;

import java.util.Objects;

public class LenderProfile {
    private Double totalLent;
    private Integer activeLoans;
    private Double averageReturn;
    private RiskTolerance riskTolerance;
    private Boolean verified;
    
    public enum RiskTolerance {
        CONSERVATIVE, MODERATE, AGGRESSIVE
    }
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public LenderProfile() {
    }
    
    public LenderProfile(Double totalLent, Integer activeLoans, Double averageReturn,
                         RiskTolerance riskTolerance, Boolean verified) {
        this.totalLent = totalLent;
        this.activeLoans = activeLoans;
        this.averageReturn = averageReturn;
        this.riskTolerance = riskTolerance;
        this.verified = verified;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public Double getTotalLent() {
        return totalLent;
    }
    
    public Integer getActiveLoans() {
        return activeLoans;
    }
    
    public Double getAverageReturn() {
        return averageReturn;
    }
    
    public RiskTolerance getRiskTolerance() {
        return riskTolerance;
    }
    
    public Boolean getVerified() {
        return verified;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setTotalLent(Double totalLent) {
        this.totalLent = totalLent;
    }
    
    public void setActiveLoans(Integer activeLoans) {
        this.activeLoans = activeLoans;
    }
    
    public void setAverageReturn(Double averageReturn) {
        this.averageReturn = averageReturn;
    }
    
    public void setRiskTolerance(RiskTolerance riskTolerance) {
        this.riskTolerance = riskTolerance;
    }
    
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
    
    // =========================================================================
    // EQUALS, HASHCODE, TOSTRING
    // =========================================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LenderProfile that = (LenderProfile) o;
        return Objects.equals(totalLent, that.totalLent) &&
               Objects.equals(activeLoans, that.activeLoans) &&
               Objects.equals(verified, that.verified);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalLent, activeLoans, verified);
    }
    
    @Override
    public String toString() {
        return "LenderProfile{" +
                "totalLent=" + totalLent +
                ", activeLoans=" + activeLoans +
                ", averageReturn=" + averageReturn +
                ", riskTolerance=" + riskTolerance +
                ", verified=" + verified +
                '}';
    }
    
    // =========================================================================
    // BUILDER
    // =========================================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Double totalLent;
        private Integer activeLoans;
        private Double averageReturn;
        private RiskTolerance riskTolerance;
        private Boolean verified;
        
        public Builder totalLent(Double totalLent) {
            this.totalLent = totalLent;
            return this;
        }
        
        public Builder activeLoans(Integer activeLoans) {
            this.activeLoans = activeLoans;
            return this;
        }
        
        public Builder averageReturn(Double averageReturn) {
            this.averageReturn = averageReturn;
            return this;
        }
        
        public Builder riskTolerance(RiskTolerance riskTolerance) {
            this.riskTolerance = riskTolerance;
            return this;
        }
        
        public Builder verified(Boolean verified) {
            this.verified = verified;
            return this;
        }
        
        public LenderProfile build() {
            return new LenderProfile(totalLent, activeLoans, averageReturn,
                                    riskTolerance, verified);
        }
    }
}
