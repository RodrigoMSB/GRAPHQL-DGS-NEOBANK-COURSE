package com.neobank.users.model;

import java.util.Objects;

public class User {
    private String id;
    private String email;
    private String fullName;
    private UserType userType;
    private LenderProfile lenderProfile;
    private BorrowerProfile borrowerProfile;
    private String createdAt;
    private Double reputation;
    
    public enum UserType {
        LENDER, BORROWER, BOTH
    }
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public User() {
    }
    
    public User(String id, String email, String fullName, UserType userType,
                LenderProfile lenderProfile, BorrowerProfile borrowerProfile,
                String createdAt, Double reputation) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.userType = userType;
        this.lenderProfile = lenderProfile;
        this.borrowerProfile = borrowerProfile;
        this.createdAt = createdAt;
        this.reputation = reputation;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public String getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public UserType getUserType() {
        return userType;
    }
    
    public LenderProfile getLenderProfile() {
        return lenderProfile;
    }
    
    public BorrowerProfile getBorrowerProfile() {
        return borrowerProfile;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public Double getReputation() {
        return reputation;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public void setUserType(UserType userType) {
        this.userType = userType;
    }
    
    public void setLenderProfile(LenderProfile lenderProfile) {
        this.lenderProfile = lenderProfile;
    }
    
    public void setBorrowerProfile(BorrowerProfile borrowerProfile) {
        this.borrowerProfile = borrowerProfile;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setReputation(Double reputation) {
        this.reputation = reputation;
    }
    
    // =========================================================================
    // EQUALS, HASHCODE, TOSTRING
    // =========================================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", userType=" + userType +
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
        private String email;
        private String fullName;
        private UserType userType;
        private LenderProfile lenderProfile;
        private BorrowerProfile borrowerProfile;
        private String createdAt;
        private Double reputation;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }
        
        public Builder userType(UserType userType) {
            this.userType = userType;
            return this;
        }
        
        public Builder lenderProfile(LenderProfile lenderProfile) {
            this.lenderProfile = lenderProfile;
            return this;
        }
        
        public Builder borrowerProfile(BorrowerProfile borrowerProfile) {
            this.borrowerProfile = borrowerProfile;
            return this;
        }
        
        public Builder createdAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder reputation(Double reputation) {
            this.reputation = reputation;
            return this;
        }
        
        public User build() {
            return new User(id, email, fullName, userType, lenderProfile,
                          borrowerProfile, createdAt, reputation);
        }
    }
}
