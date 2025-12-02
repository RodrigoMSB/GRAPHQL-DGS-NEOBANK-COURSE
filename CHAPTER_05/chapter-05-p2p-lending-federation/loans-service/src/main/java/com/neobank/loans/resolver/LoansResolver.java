package com.neobank.loans.resolver;

import com.neobank.loans.model.Loan;
import com.neobank.loans.service.LoansService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DgsComponent
@RequiredArgsConstructor
public class LoansResolver {
    
    private final LoansService loansService;
    
    @DgsQuery
    public Loan loan(@InputArgument String id) {
        return loansService.getLoanById(id);
    }
    
    @DgsQuery
    public List<Loan> loans() {
        return loansService.getAllLoans();
    }
    
    @DgsQuery
    public List<Loan> loansByStatus(@InputArgument String status) {
        return loansService.getLoansByStatus(Loan.LoanStatus.valueOf(status));
    }
    
    @DgsQuery
    public List<Loan> availableLoans() {
        return loansService.getAvailableLoans();
    }
    
    /**
     * Resolver para User.loansAsLender
     * Parte de la extensión del tipo User desde el subgrafo Loans
     */
    @DgsData(parentType = "User", field = "loansAsLender")
    public List<Loan> loansAsLender(Map<String, Object> user) {
        String userId = (String) user.get("id");
        return loansService.getLoansByLender(userId);
    }
    
    /**
     * Resolver para User.loansAsBorrower
     * Parte de la extensión del tipo User desde el subgrafo Loans
     */
    @DgsData(parentType = "User", field = "loansAsBorrower")
    public List<Loan> loansAsBorrower(Map<String, Object> user) {
        String userId = (String) user.get("id");
        return loansService.getLoansByBorrower(userId);
    }
    
    /**
     * Resolver para Loan.lender
     * Retorna una referencia stub que será resuelta por el subgrafo Users
     */
    @DgsData(parentType = "Loan", field = "lender")
    public Map<String, Object> lender(Loan loan) {
        Map<String, Object> userRef = new HashMap<>();
        userRef.put("__typename", "User");
        userRef.put("id", loan.getLenderId());
        return userRef;
    }
    
    /**
     * Resolver para Loan.borrower
     * Retorna una referencia stub que será resuelta por el subgrafo Users
     */
    @DgsData(parentType = "Loan", field = "borrower")
    public Map<String, Object> borrower(Loan loan) {
        Map<String, Object> userRef = new HashMap<>();
        userRef.put("__typename", "User");
        userRef.put("id", loan.getBorrowerId());
        return userRef;
    }
    
    @DgsMutation
    public Map<String, Object> createLoanRequest(@InputArgument Map<String, Object> input) {
        try {
            String borrowerId = input.get("borrowerId").toString();
            Double amount = Double.parseDouble(input.get("amount").toString());
            Double interestRate = Double.parseDouble(input.get("interestRate").toString());
            Integer term = Integer.parseInt(input.get("term").toString());
            String purpose = input.get("purpose").toString();
            
            Loan loan = loansService.createLoanRequest(borrowerId, amount, interestRate, term, purpose);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan request created successfully");
            response.put("loan", loan);
            return response;
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("loan", null);
            return response;
        }
    }
    
    @DgsMutation
    public Map<String, Object> fundLoan(@InputArgument String loanId, @InputArgument String lenderId) {
        try {
            Loan loan = loansService.fundLoan(loanId, lenderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan funded successfully");
            response.put("loan", loan);
            return response;
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("loan", null);
            return response;
        }
    }
}
