#!/bin/bash

cd /home/claude/chapter02

# UserQueryResolver
cat > src/main/java/com/neobank/cashback/graphql/UserQueryResolver.java << 'EOF'
package com.neobank.cashback.graphql;

import com.neobank.cashback.model.*;
import com.neobank.cashback.service.CashbackService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class UserQueryResolver {
    
    private final CashbackService cashbackService;
    
    public UserQueryResolver(CashbackService cashbackService) {
        this.cashbackService = cashbackService;
    }
    
    @QueryMapping
    public User user(@Argument String id) {
        return cashbackService.getUserById(id);
    }
    
    @QueryMapping
    public User userByEmail(@Argument String email) {
        return cashbackService.getUserByEmail(email);
    }
    
    @QueryMapping
    public List<User> users(@Argument CashbackTier tier) {
        List<User> allUsers = cashbackService.getAllUsers();
        if (tier == null) {
            return allUsers;
        }
        return allUsers.stream()
            .filter(u -> u.getTier() == tier)
            .toList();
    }
    
    // CAMPOS CALCULADOS DEL USER
    // Estos se resuelven cuando el cliente los pide
    
    @SchemaMapping(typeName = "User")
    public List<Transaction> transactions(User user) {
        return cashbackService.getTransactionsByUserId(user.getId());
    }
    
    @SchemaMapping(typeName = "User")
    public List<Reward> rewards(User user) {
        return cashbackService.getRewardsByUserId(user.getId());
    }
    
    @SchemaMapping(typeName = "User")
    public Double availableCashback(User user) {
        return cashbackService.calculateAvailableCashback(user.getId());
    }
    
    @SchemaMapping(typeName = "User")
    public Double totalCashbackEarned(User user) {
        return cashbackService.calculateTotalCashbackEarned(user.getId());
    }
    
    @SchemaMapping(typeName = "User")
    public Double totalSpent(User user) {
        return cashbackService.calculateTotalSpent(user.getId());
    }
}
EOF

# TransactionQueryResolver
cat > src/main/java/com/neobank/cashback/graphql/TransactionQueryResolver.java << 'EOF'
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
        
        // Filtrar por status
        if (status != null) {
            txs = txs.stream()
                .filter(t -> t.getStatus() == status)
                .toList();
        }
        
        // Filtrar por categoría
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
EOF

# TransactionMutationResolver
cat > src/main/java/com/neobank/cashback/graphql/TransactionMutationResolver.java << 'EOF'
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
EOF

# Response types
mkdir -p src/main/java/com/neobank/cashback/model/response
cat > src/main/java/com/neobank/cashback/model/response/TransactionResponse.java << 'EOF'
package com.neobank.cashback.model.response;

import com.neobank.cashback.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Boolean success;
    private String message;
    private Transaction transaction;
}
EOF

echo "Resolvers creados exitosamente"
