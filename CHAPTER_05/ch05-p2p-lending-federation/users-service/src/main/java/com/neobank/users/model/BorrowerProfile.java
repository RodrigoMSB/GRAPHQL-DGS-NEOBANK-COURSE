package com.neobank.users.model;

import java.util.Objects;

public class BorrowerProfile {
    private Integer creditScore;
    private Double totalBorrowed;
    private Integer activeLoans;
    private Double defaultRate;
    private Boolean verified;
    private KYCStatus kycStatus;
    
    public enum KYCStatus {
        PENDING, VERIFIED, REJECTED
    }
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public BorrowerProfile() {
    }
    
    public BorrowerProfile(Integer creditScore, Double totalBorrowed, Integer activeLoans,
                           Double defaultRate, Boolean verified, KYCStatus kycStatus) {
        this.creditScore = creditScore;
        this.totalBorrowed = totalBorrowed;
        this.activeLoans = activeLoans;
        this.defaultRate = defaultRate;
        this.verified = verified;
        this.kycStatus = kycStatus;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public Integer getCreditScore() {
        return creditScore;
    }
    
    public Double getTotalBorrowed() {
        return totalBorrowed;
    }
    
    public Integer getActiveLoans() {
        return activeLoans;
    }
    
    public Double getDefaultRate() {
        return defaultRate;
    }
    
    public Boolean getVerified() {
        return verified;
    }
    
    public KYCStatus getKycStatus() {
        return kycStatus;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }
    
    public void setTotalBorrowed(Double totalBorrowed) {
        this.totalBorrowed = totalBorrowed;
    }
    
    public void setActiveLoans(Integer activeLoans) {
        this.activeLoans = activeLoans;
    }
    
    public void setDefaultRate(Double defaultRate) {
        this.defaultRate = defaultRate;
    }
    
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
    
    public void setKycStatus(KYCStatus kycStatus) {
        this.kycStatus = kycStatus;
    }
    
    // =========================================================================
    // EQUALS, HASHCODE, TOSTRING
    // =========================================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BorrowerProfile that = (BorrowerProfile) o;
        return Objects.equals(creditScore, that.creditScore) &&
               Objects.equals(totalBorrowed, that.totalBorrowed) &&
               Objects.equals(verified, that.verified);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(creditScore, totalBorrowed, verified);
    }
    
    @Override
    public String toString() {
        return "BorrowerProfile{" +
                "creditScore=" + creditScore +
                ", totalBorrowed=" + totalBorrowed +
                ", activeLoans=" + activeLoans +
                ", defaultRate=" + defaultRate +
                ", verified=" + verified +
                ", kycStatus=" + kycStatus +
                '}';
    }
    
    // =========================================================================
    // BUILDER
    // =========================================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Integer creditScore;
        private Double totalBorrowed;
        private Integer activeLoans;
        private Double defaultRate;
        private Boolean verified;
        private KYCStatus kycStatus;
        
        public Builder creditScore(Integer creditScore) {
            this.creditScore = creditScore;
            return this;
        }
        
        public Builder totalBorrowed(Double totalBorrowed) {
            this.totalBorrowed = totalBorrowed;
            return this;
        }
        
        public Builder activeLoans(Integer activeLoans) {
            this.activeLoans = activeLoans;
            return this;
        }
        
        public Builder defaultRate(Double defaultRate) {
            this.defaultRate = defaultRate;
            return this;
        }
        
        public Builder verified(Boolean verified) {
            this.verified = verified;
            return this;
        }
        
        public Builder kycStatus(KYCStatus kycStatus) {
            this.kycStatus = kycStatus;
            return this;
        }
        
        public BorrowerProfile build() {
            return new BorrowerProfile(creditScore, totalBorrowed, activeLoans,
                                      defaultRate, verified, kycStatus);
        }
    }
}
