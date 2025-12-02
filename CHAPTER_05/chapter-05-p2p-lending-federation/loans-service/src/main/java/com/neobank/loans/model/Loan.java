package com.neobank.loans.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
