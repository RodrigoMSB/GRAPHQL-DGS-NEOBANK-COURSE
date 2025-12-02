package com.neobank.cashback.graphql;

import com.neobank.cashback.model.Transaction;
import com.neobank.cashback.model.input.CreateTransactionInput;
import com.neobank.cashback.model.response.TransactionResponse;
import com.neobank.cashback.service.CashbackService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class TransactionMutationResolver {
    
    private final CashbackService cashbackService;
    
    public TransactionMutationResolver(CashbackService cashbackService) {
        this.cashbackService = cashbackService;
    }
    
    @MutationMapping
    public TransactionResponse createTransaction(@Argument CreateTransactionInput input) {
        try {
            Transaction transaction = cashbackService.createTransaction(
                input.getUserId(),
                input.getAmount(),
                input.getCategory(),
                input.getMerchantName(),
                input.getDescription(),
                input.getTransactionDate()
            );
            
            // Auto-confirmar para el demo
            transaction = cashbackService.confirmTransaction(transaction.getId());
            
            return TransactionResponse.builder()
                .success(true)
                .message("Transaction created and confirmed successfully")
                .transaction(transaction)
                .build();
                
        } catch (Exception e) {
            return TransactionResponse.builder()
                .success(false)
                .message("Error creating transaction: " + e.getMessage())
                .build();
        }
    }
}
