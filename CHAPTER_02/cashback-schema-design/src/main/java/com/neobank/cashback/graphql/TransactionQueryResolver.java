package com.neobank.cashback.graphql;

import com.neobank.cashback.model.*;
import com.neobank.cashback.service.CashbackService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class TransactionQueryResolver {
    
    private final CashbackService cashbackService;
    
    public TransactionQueryResolver(CashbackService cashbackService) {
        this.cashbackService = cashbackService;
    }
    
    @QueryMapping
    public Transaction transaction(@Argument String id) {
        return cashbackService.getTransactionById(id);
    }
    
    @QueryMapping
    public List<Transaction> transactions(
        @Argument String userId,
        @Argument TransactionStatus status,
        @Argument TransactionCategory category
    ) {
        List<Transaction> txs;
        
        if (userId != null) {
            txs = cashbackService.getTransactionsByUserId(userId);
        } else {
            txs = cashbackService.getAllTransactions();
        }
        
        if (status != null) {
            txs = txs.stream()
                .filter(t -> t.getStatus() == status)
                .toList();
        }
        
        if (category != null) {
            txs = txs.stream()
                .filter(t -> t.getCategory() == category)
                .toList();
        }
        
        return txs;
    }
    
    // CAMPOS CALCULADOS DE TRANSACTION
    
    @SchemaMapping(typeName = "Transaction")
    public User user(Transaction transaction) {
        return cashbackService.getUserById(transaction.getUserId());
    }
    
    @SchemaMapping(typeName = "Transaction")
    public Double cashbackAmount(Transaction transaction) {
        return cashbackService.calculateCashbackAmount(transaction);
    }
    
    @SchemaMapping(typeName = "Transaction")
    public Double cashbackPercentage(Transaction transaction) {
        return cashbackService.calculateCashbackPercentage(transaction);
    }
}
