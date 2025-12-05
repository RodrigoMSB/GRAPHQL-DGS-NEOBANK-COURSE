package com.neobank.carbon.service;

import com.neobank.carbon.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransactionService {
    
    private final CarbonCalculatorService carbonCalculator;
    private final ESGService esgService;
    
    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();
    private final Map<String, List<Transaction>> transactionsByAccount = new ConcurrentHashMap<>();
    
    public TransactionService(CarbonCalculatorService carbonCalculator, ESGService esgService) {
        this.carbonCalculator = carbonCalculator;
        this.esgService = esgService;
        initializeSampleData();
    }
    
    public Transaction createTransaction(String accountId, Double amount, String currency,
                                        String merchantName, MerchantCategory merchantCategory,
                                        LocalDate date) {
        
        String id = "txn-" + UUID.randomUUID().toString().substring(0, 8);
        
        // Calcular carbon footprint
        CarbonFootprint footprint = carbonCalculator.calculateFootprint(
            amount, merchantCategory, merchantName
        );
        
        // Obtener ESG score del merchant
        ESGScore esgScore = esgService.getESGScore(merchantName, merchantCategory);
        
        Transaction transaction = Transaction.builder()
                .id(id)
                .accountId(accountId)
                .amount(amount)
                .currency(currency)
                .merchantName(merchantName)
                .merchantCategory(merchantCategory)
                .date(date)
                .carbonFootprint(footprint)
                .esgScore(esgScore)
                // Deprecated fields (for backward compatibility)
                .category(merchantCategory.name())
                .hasOffset(footprint.getOffsetPurchased())
                .build();
        
        transactions.put(id, transaction);
        transactionsByAccount.computeIfAbsent(accountId, k -> new ArrayList<>()).add(transaction);
        
        log.info("Transaction created: {} - {} at {} (CO2: {} kg)", 
                id, amount, merchantName, footprint.getCo2Kg());
        
        return transaction;
    }
    
    public Transaction getTransactionById(String id) {
        return transactions.get(id);
    }
    
    public List<Transaction> getTransactionsByAccount(String accountId) {
        return transactionsByAccount.getOrDefault(accountId, new ArrayList<>());
    }
    
    public List<Transaction> getTransactionsByImpact(String accountId, ImpactLevel impactLevel) {
        return getTransactionsByAccount(accountId).stream()
                .filter(t -> t.getCarbonFootprint().getImpactLevel() == impactLevel)
                .collect(Collectors.toList());
    }
    
    public List<Transaction> getTransactionsByMonth(String accountId, int year, int month) {
        return getTransactionsByAccount(accountId).stream()
                .filter(t -> t.getDate().getYear() == year && t.getDate().getMonthValue() == month)
                .collect(Collectors.toList());
    }
    
    public boolean purchaseOffset(String transactionId) {
        Transaction transaction = transactions.get(transactionId);
        if (transaction != null && !transaction.getCarbonFootprint().getOffsetPurchased()) {
            CarbonFootprint footprint = transaction.getCarbonFootprint();
            footprint.setOffsetPurchased(true);
            footprint.setOffsetCost(footprint.getCo2Kg() * 15.0); // $15 per kg CO2
            
            // Update deprecated field
            transaction.setHasOffset(true);
            
            log.info("Carbon offset purchased for transaction {}: ${}", 
                    transactionId, footprint.getOffsetCost());
            return true;
        }
        return false;
    }
    
    private void initializeSampleData() {
        // Account 001 - Noviembre 2024
        createTransaction("account-001", 150.0, "USD", "Whole Foods", 
                MerchantCategory.FOOD_RETAIL, LocalDate.of(2024, 11, 1));
        
        createTransaction("account-001", 500.0, "USD", "United Airlines", 
                MerchantCategory.TRAVEL_AVIATION, LocalDate.of(2024, 11, 5));
        
        createTransaction("account-001", 80.0, "USD", "Zara", 
                MerchantCategory.FASHION_RETAIL, LocalDate.of(2024, 11, 10));
        
        createTransaction("account-001", 1200.0, "USD", "Tesla Supercharger", 
                MerchantCategory.ENERGY, LocalDate.of(2024, 11, 15));
        
        createTransaction("account-001", 45.0, "USD", "Uber", 
                MerchantCategory.TRANSPORTATION, LocalDate.of(2024, 11, 20));
        
        // Account 001 - Diciembre 2024
        createTransaction("account-001", 200.0, "USD", "Whole Foods", 
                MerchantCategory.FOOD_RETAIL, LocalDate.of(2024, 12, 1));
        
        createTransaction("account-001", 2500.0, "USD", "Emirates Airlines", 
                MerchantCategory.TRAVEL_AVIATION, LocalDate.of(2024, 12, 5));
        
        log.info("Initialized {} sample transactions", transactions.size());
    }
}
