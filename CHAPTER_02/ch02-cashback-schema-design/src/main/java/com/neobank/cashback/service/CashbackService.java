package com.neobank.cashback.service;

import com.neobank.cashback.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CashbackService {
    
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Transaction> transactions = new HashMap<>();
    private final Map<String, Reward> rewards = new HashMap<>();
    
    public CashbackService() {
        initMockData();
    }
    
    private void initMockData() {
        // User 1: Maria Silva (GOLD tier)
        User user1 = User.builder()
            .id("user-001")
            .email("maria.silva@email.com")
            .fullName("Maria Silva")
            .tier(CashbackTier.GOLD)
            .enrolledAt(LocalDateTime.now().minusMonths(6))
            .build();
        users.put(user1.getId(), user1);
        
        // User 2: Carlos Rodriguez (PLATINUM tier)
        User user2 = User.builder()
            .id("user-002")
            .email("carlos.rodriguez@email.com")
            .fullName("Carlos Rodriguez")
            .tier(CashbackTier.PLATINUM)
            .enrolledAt(LocalDateTime.now().minusYears(2))
            .build();
        users.put(user2.getId(), user2);
        
        // Transactions para user1
        createMockTransaction("trans-001", "user-001", 150.0, TransactionCategory.GROCERIES, "Supermarket XYZ");
        createMockTransaction("trans-002", "user-001", 85.50, TransactionCategory.RESTAURANTS, "Pizza House");
        createMockTransaction("trans-003", "user-001", 1200.0, TransactionCategory.TRAVEL, "Airline Tickets");
        
        // Transactions para user2
        createMockTransaction("trans-004", "user-002", 60.0, TransactionCategory.GAS_STATIONS, "Shell Station");
        createMockTransaction("trans-005", "user-002", 450.0, TransactionCategory.SHOPPING, "Electronics Store");
    }
    
    private void createMockTransaction(String id, String userId, Double amount, 
                                      TransactionCategory category, String merchant) {
        Transaction tx = Transaction.builder()
            .id(id)
            .userId(userId)
            .amount(amount)
            .category(category)
            .merchantName(merchant)
            .transactionDate(LocalDateTime.now().minusDays(new Random().nextInt(30)))
            .status(TransactionStatus.CONFIRMED)
            .build();
        transactions.put(id, tx);
        
        // Crear reward automáticamente
        User user = users.get(userId);
        double cashbackPercentage = user.getTier().getCashbackPercentage(category);
        double cashbackAmount = amount * (cashbackPercentage / 100.0);
        
        Reward reward = Reward.builder()
            .id("reward-" + id)
            .userId(userId)
            .transactionId(id)
            .amount(cashbackAmount)
            .earnedAt(tx.getTransactionDate())
            .expiresAt(tx.getTransactionDate().plusMonths(12))
            .status(RewardStatus.AVAILABLE)
            .build();
        rewards.put(reward.getId(), reward);
    }
    
    public User getUserById(String id) {
        return users.get(id);
    }
    
    public User getUserByEmail(String email) {
        return users.values().stream()
            .filter(u -> u.getEmail().equals(email))
            .findFirst()
            .orElse(null);
    }
    
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    public Transaction getTransactionById(String id) {
        return transactions.get(id);
    }
    
    public List<Transaction> getTransactionsByUserId(String userId) {
        return transactions.values().stream()
            .filter(t -> t.getUserId().equals(userId))
            .collect(Collectors.toList());
    }
    
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values());
    }
    
    public Reward getRewardById(String id) {
        return rewards.get(id);
    }
    
    public List<Reward> getRewardsByUserId(String userId) {
        return rewards.values().stream()
            .filter(r -> r.getUserId().equals(userId))
            .collect(Collectors.toList());
    }
    
    public Double calculateAvailableCashback(String userId) {
        return rewards.values().stream()
            .filter(r -> r.getUserId().equals(userId))
            .filter(r -> r.getStatus() == RewardStatus.AVAILABLE)
            .mapToDouble(Reward::getAmount)
            .sum();
    }
    
    public Double calculateTotalCashbackEarned(String userId) {
        return rewards.values().stream()
            .filter(r -> r.getUserId().equals(userId))
            .mapToDouble(Reward::getAmount)
            .sum();
    }
    
    public Double calculateTotalSpent(String userId) {
        return transactions.values().stream()
            .filter(t -> t.getUserId().equals(userId))
            .filter(t -> t.getStatus() == TransactionStatus.CONFIRMED)
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
    
    public Double calculateCashbackAmount(Transaction transaction) {
        User user = users.get(transaction.getUserId());
        if (user == null) return 0.0;
        
        double percentage = user.getTier().getCashbackPercentage(transaction.getCategory());
        return transaction.getAmount() * (percentage / 100.0);
    }
    
    public Double calculateCashbackPercentage(Transaction transaction) {
        User user = users.get(transaction.getUserId());
        if (user == null) return 0.0;
        
        return user.getTier().getCashbackPercentage(transaction.getCategory());
    }
    
    public Transaction createTransaction(String userId, Double amount, 
                                        TransactionCategory category, String merchantName,
                                        String description, LocalDateTime date) {
        String id = "trans-" + UUID.randomUUID().toString().substring(0, 8);
        
        Transaction transaction = Transaction.builder()
            .id(id)
            .userId(userId)
            .amount(amount)
            .category(category)
            .merchantName(merchantName)
            .description(description)
            .transactionDate(date != null ? date : LocalDateTime.now())
            .status(TransactionStatus.PENDING)
            .build();
        
        transactions.put(id, transaction);
        return transaction;
    }
    
    public Transaction confirmTransaction(String transactionId) {
        Transaction tx = transactions.get(transactionId);
        if (tx == null) return null;
        
        tx.setStatus(TransactionStatus.CONFIRMED);
        
        // Crear reward
        User user = users.get(tx.getUserId());
        double cashbackAmount = calculateCashbackAmount(tx);
        
        Reward reward = Reward.builder()
            .id("reward-" + UUID.randomUUID().toString().substring(0, 8))
            .userId(tx.getUserId())
            .transactionId(tx.getId())
            .amount(cashbackAmount)
            .earnedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusMonths(12))
            .status(RewardStatus.PENDING)
            .build();
        
        rewards.put(reward.getId(), reward);
        return tx;
    }
}
