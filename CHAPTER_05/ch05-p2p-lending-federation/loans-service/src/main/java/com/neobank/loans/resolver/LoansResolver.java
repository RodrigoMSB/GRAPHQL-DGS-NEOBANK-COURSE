package com.neobank.loans.resolver;

import com.neobank.loans.model.Loan;
import com.neobank.loans.service.LoansService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import graphql.schema.DataFetchingEnvironment;
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
    
    @DgsData(parentType = "User", field = "loansAsLender")
    public List<Loan> loansAsLender(DataFetchingEnvironment dfe) {
        Map<String, Object> user = dfe.getSource();
        String userId = (String) user.get("id");
        if (userId == null) return List.of();
        return loansService.getLoansByLender(userId);
    }
    
    @DgsData(parentType = "User", field = "loansAsBorrower")
    public List<Loan> loansAsBorrower(DataFetchingEnvironment dfe) {
        Map<String, Object> user = dfe.getSource();
        String userId = (String) user.get("id");
        if (userId == null) return List.of();
        return loansService.getLoansByBorrower(userId);
    }
    
    @DgsData(parentType = "Loan", field = "lender")
    public Map<String, Object> lender(DataFetchingEnvironment dfe) {
        Loan loan = dfe.getSource();
        if (loan == null || loan.getLenderId() == null) return null;
        Map<String, Object> userRef = new HashMap<>();
        userRef.put("__typename", "User");
        userRef.put("id", loan.getLenderId());
        return userRef;
    }
    
    @DgsData(parentType = "Loan", field = "borrower")
    public Map<String, Object> borrower(DataFetchingEnvironment dfe) {
        Loan loan = dfe.getSource();
        if (loan == null || loan.getBorrowerId() == null) return null;
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