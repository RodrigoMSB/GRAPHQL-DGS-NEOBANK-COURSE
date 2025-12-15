package com.neobank.fraud.service;

import com.neobank.fraud.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    
    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();
    private final Map<String, List<Transaction>> transactionsByAccount = new ConcurrentHashMap<>();
    
    public TransactionService() {
        // Inicializar con datos de ejemplo
        initializeSampleData();
    }
    
    public Transaction createTransaction(String accountId, Double amount, String currency,
                                        String merchantName, String category, String location) {
        
        String id = "txn-" + UUID.randomUUID().toString().substring(0, 8);
        
        Transaction transaction = Transaction.builder()
                .id(id)
                .accountId(accountId)
                .amount(amount)
                .currency(currency)
                .merchantName(merchantName)
                .category(category)
                .location(location)
                .timestamp(LocalDateTime.now())
                .riskScore(0.0)
                .status(Transaction.TransactionStatus.PENDING)
                .build();
        
        transactions.put(id, transaction);
        transactionsByAccount.computeIfAbsent(accountId, k -> new ArrayList<>()).add(transaction);
        
        log.info("Transaction created: {} - {} {} at {}", id, amount, currency, merchantName);
        
        return transaction;
    }
    
    public Transaction getTransactionById(String id) {
        return transactions.get(id);
    }
    
    public List<Transaction> getTransactionsByAccount(String accountId) {
        return transactionsByAccount.getOrDefault(accountId, new ArrayList<>());
    }
    
    public void updateTransactionStatus(String id, Transaction.TransactionStatus status) {
        Transaction transaction = transactions.get(id);
        if (transaction != null) {
            transaction.setStatus(status);
            log.info("Transaction {} status updated to {}", id, status);
        }
    }
    
    public void updateTransactionRiskScore(String id, Double riskScore) {
        Transaction transaction = transactions.get(id);
        if (transaction != null) {
            transaction.setRiskScore(riskScore);
            log.info("Transaction {} risk score updated to {}", id, riskScore);
        }
    }
    
    public Double getAverageTransactionAmount(String accountId) {
        List<Transaction> accountTransactions = getTransactionsByAccount(accountId);
        if (accountTransactions.isEmpty()) {
            return 100.0; // Default
        }
        return accountTransactions.stream()
                .mapToDouble(Transaction::getAmount)
                .average()
                .orElse(100.0);
    }
    
    public List<Transaction> getRecentTransactions(String accountId, int minutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        return getTransactionsByAccount(accountId).stream()
                .filter(t -> t.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
    }
    
    private void initializeSampleData() {
        // Transacciones normales para account-001
        createTransaction("account-001", 45.50, "USD", "Starbucks", "Food & Drink", "San Francisco, US");
        createTransaction("account-001", 120.00, "USD", "Amazon", "Shopping", "San Francisco, US");
        createTransaction("account-001", 35.20, "USD", "Uber", "Transportation", "San Francisco, US");
        
        // Transacciones normales para account-002
        createTransaction("account-002", 250.00, "USD", "Whole Foods", "Groceries", "New York, US");
        createTransaction("account-002", 80.00, "USD", "Netflix", "Entertainment", "New York, US");
    }
}
