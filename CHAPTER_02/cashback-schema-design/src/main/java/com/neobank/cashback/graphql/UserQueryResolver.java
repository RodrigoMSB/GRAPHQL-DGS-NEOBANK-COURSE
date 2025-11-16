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
