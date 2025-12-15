package com.neobank.loans.model;

import java.util.Objects;

public class Loan {
    private String id;
    private Double amount;
    private Double interestRate;
    private Integer term;
    private LoanStatus status;
    private String purpose;
    private String lenderId;
    private String borrowerId;
    private String createdAt;
    private String fundedAt;
    private Double monthlyPayment;
    private Double totalRepayment;
    
    public enum LoanStatus {
        PENDING, FUNDED, ACTIVE, COMPLETED, DEFAULTED
    }
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public Loan() {
    }
    
    public Loan(String id, Double amount, Double interestRate, Integer term,
                LoanStatus status, String purpose, String lenderId, String borrowerId,
                String createdAt, String fundedAt, Double monthlyPayment, Double totalRepayment) {
        this.id = id;
        this.amount = amount;
        this.interestRate = interestRate;
        this.term = term;
        this.status = status;
        this.purpose = purpose;
        this.lenderId = lenderId;
        this.borrowerId = borrowerId;
        this.createdAt = createdAt;
        this.fundedAt = fundedAt;
        this.monthlyPayment = monthlyPayment;
        this.totalRepayment = totalRepayment;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public String getId() {
        return id;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public Double getInterestRate() {
        return interestRate;
    }
    
    public Integer getTerm() {
        return term;
    }
    
    public LoanStatus getStatus() {
        return status;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public String getLenderId() {
        return lenderId;
    }
    
    public String getBorrowerId() {
        return borrowerId;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public String getFundedAt() {
        return fundedAt;
    }
    
    public Double getMonthlyPayment() {
        return monthlyPayment;
    }
    
    public Double getTotalRepayment() {
        return totalRepayment;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }
    
    public void setTerm(Integer term) {
        this.term = term;
    }
    
    public void setStatus(LoanStatus status) {
        this.status = status;
    }
    
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    public void setLenderId(String lenderId) {
        this.lenderId = lenderId;
    }
    
    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setFundedAt(String fundedAt) {
        this.fundedAt = fundedAt;
    }
    
    public void setMonthlyPayment(Double monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }
    
    public void setTotalRepayment(Double totalRepayment) {
        this.totalRepayment = totalRepayment;
    }
    
    // =========================================================================
    // EQUALS, HASHCODE, TOSTRING
    // =========================================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loan loan = (Loan) o;
        return Objects.equals(id, loan.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Loan{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                ", borrowerId='" + borrowerId + '\'' +
                ", lenderId='" + lenderId + '\'' +
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
        private Double amount;
        private Double interestRate;
        private Integer term;
        private LoanStatus status;
        private String purpose;
        private String lenderId;
        private String borrowerId;
        private String createdAt;
        private String fundedAt;
        private Double monthlyPayment;
        private Double totalRepayment;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder interestRate(Double interestRate) {
            this.interestRate = interestRate;
            return this;
        }
        
        public Builder term(Integer term) {
            this.term = term;
            return this;
        }
        
        public Builder status(LoanStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder purpose(String purpose) {
            this.purpose = purpose;
            return this;
        }
        
        public Builder lenderId(String lenderId) {
            this.lenderId = lenderId;
            return this;
        }
        
        public Builder borrowerId(String borrowerId) {
            this.borrowerId = borrowerId;
            return this;
        }
        
        public Builder createdAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder fundedAt(String fundedAt) {
            this.fundedAt = fundedAt;
            return this;
        }
        
        public Builder monthlyPayment(Double monthlyPayment) {
            this.monthlyPayment = monthlyPayment;
            return this;
        }
        
        public Builder totalRepayment(Double totalRepayment) {
            this.totalRepayment = totalRepayment;
            return this;
        }
        
        public Loan build() {
            return new Loan(id, amount, interestRate, term, status, purpose,
                          lenderId, borrowerId, createdAt, fundedAt,
                          monthlyPayment, totalRepayment);
        }
    }
}
