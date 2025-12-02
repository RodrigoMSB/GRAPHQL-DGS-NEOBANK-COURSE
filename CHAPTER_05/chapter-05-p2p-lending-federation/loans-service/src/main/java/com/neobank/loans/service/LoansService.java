package com.neobank.loans.service;

import com.neobank.loans.model.Loan;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoansService {
    
    private final Map<String, Loan> loans = new HashMap<>();
    
    public LoansService() {
        initializeData();
    }
    
    private void initializeData() {
        // Loan 1: ACTIVE (funded by user-001, borrowed by user-003)
        loans.put("loan-001", Loan.builder()
            .id("loan-001")
            .amount(25000.0)
            .interestRate(7.5)
            .term(36)
            .status(Loan.LoanStatus.ACTIVE)
            .purpose("Business expansion")
            .lenderId("user-001")
            .borrowerId("user-003")
            .createdAt(LocalDateTime.now().minusMonths(6).toString())
            .fundedAt(LocalDateTime.now().minusMonths(5).toString())
            .monthlyPayment(calculateMonthlyPayment(25000.0, 7.5, 36))
            .totalRepayment(calculateTotalRepayment(25000.0, 7.5, 36))
            .build());
        
        // Loan 2: ACTIVE (funded by user-002, borrowed by user-004)
        loans.put("loan-002", Loan.builder()
            .id("loan-002")
            .amount(15000.0)
            .interestRate(8.2)
            .term(24)
            .status(Loan.LoanStatus.ACTIVE)
            .purpose("Equipment purchase")
            .lenderId("user-002")
            .borrowerId("user-004")
            .createdAt(LocalDateTime.now().minusMonths(3).toString())
            .fundedAt(LocalDateTime.now().minusMonths(2).toString())
            .monthlyPayment(calculateMonthlyPayment(15000.0, 8.2, 24))
            .totalRepayment(calculateTotalRepayment(15000.0, 8.2, 24))
            .build());
        
        // Loan 3: PENDING (not funded yet)
        loans.put("loan-003", Loan.builder()
            .id("loan-003")
            .amount(30000.0)
            .interestRate(9.0)
            .term(48)
            .status(Loan.LoanStatus.PENDING)
            .purpose("Home renovation")
            .lenderId(null)
            .borrowerId("user-003")
            .createdAt(LocalDateTime.now().minusDays(5).toString())
            .fundedAt(null)
            .monthlyPayment(calculateMonthlyPayment(30000.0, 9.0, 48))
            .totalRepayment(calculateTotalRepayment(30000.0, 9.0, 48))
            .build());
        
        // Loan 4: COMPLETED
        loans.put("loan-004", Loan.builder()
            .id("loan-004")
            .amount(10000.0)
            .interestRate(6.5)
            .term(12)
            .status(Loan.LoanStatus.COMPLETED)
            .purpose("Inventory purchase")
            .lenderId("user-001")
            .borrowerId("user-005")
            .createdAt(LocalDateTime.now().minusYears(2).toString())
            .fundedAt(LocalDateTime.now().minusYears(2).plusDays(3).toString())
            .monthlyPayment(calculateMonthlyPayment(10000.0, 6.5, 12))
            .totalRepayment(calculateTotalRepayment(10000.0, 6.5, 12))
            .build());
        
        // Loan 5: PENDING
        loans.put("loan-005", Loan.builder()
            .id("loan-005")
            .amount(20000.0)
            .interestRate(8.5)
            .term(36)
            .status(Loan.LoanStatus.PENDING)
            .purpose("Marketing campaign")
            .lenderId(null)
            .borrowerId("user-004")
            .createdAt(LocalDateTime.now().minusDays(2).toString())
            .fundedAt(null)
            .monthlyPayment(calculateMonthlyPayment(20000.0, 8.5, 36))
            .totalRepayment(calculateTotalRepayment(20000.0, 8.5, 36))
            .build());
    }
    
    private Double calculateMonthlyPayment(Double amount, Double annualRate, Integer term) {
        double monthlyRate = (annualRate / 100) / 12;
        return amount * (monthlyRate * Math.pow(1 + monthlyRate, term)) / 
               (Math.pow(1 + monthlyRate, term) - 1);
    }
    
    private Double calculateTotalRepayment(Double amount, Double annualRate, Integer term) {
        return calculateMonthlyPayment(amount, annualRate, term) * term;
    }
    
    public Loan getLoanById(String id) {
        return loans.get(id);
    }
    
    public List<Loan> getAllLoans() {
        return new ArrayList<>(loans.values());
    }
    
    public List<Loan> getLoansByStatus(Loan.LoanStatus status) {
        return loans.values().stream()
            .filter(loan -> loan.getStatus() == status)
            .collect(Collectors.toList());
    }
    
    public List<Loan> getAvailableLoans() {
        return getLoansByStatus(Loan.LoanStatus.PENDING);
    }
    
    public List<Loan> getLoansByLender(String lenderId) {
        return loans.values().stream()
            .filter(loan -> lenderId.equals(loan.getLenderId()))
            .collect(Collectors.toList());
    }
    
    public List<Loan> getLoansByBorrower(String borrowerId) {
        return loans.values().stream()
            .filter(loan -> borrowerId.equals(loan.getBorrowerId()))
            .collect(Collectors.toList());
    }
    
    public Loan createLoanRequest(String borrowerId, Double amount, Double interestRate, 
                                   Integer term, String purpose) {
        String id = "loan-" + String.format("%03d", loans.size() + 1);
        Loan loan = Loan.builder()
            .id(id)
            .amount(amount)
            .interestRate(interestRate)
            .term(term)
            .status(Loan.LoanStatus.PENDING)
            .purpose(purpose)
            .borrowerId(borrowerId)
            .createdAt(LocalDateTime.now().toString())
            .monthlyPayment(calculateMonthlyPayment(amount, interestRate, term))
            .totalRepayment(calculateTotalRepayment(amount, interestRate, term))
            .build();
        
        loans.put(id, loan);
        return loan;
    }
    
    public Loan fundLoan(String loanId, String lenderId) {
        Loan loan = loans.get(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan not found");
        }
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new IllegalStateException("Loan is not pending");
        }
        
        loan.setLenderId(lenderId);
        loan.setStatus(Loan.LoanStatus.FUNDED);
        loan.setFundedAt(LocalDateTime.now().toString());
        
        return loan;
    }
}
