package com.neobank.analytics.service;

import com.neobank.analytics.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    
    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);
    
    private final ExpenseService expenseService;
    
    public AnalyticsService(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }
    
    /**
     * RESOLVER-LEVEL CACHING
     * Cachea el resumen completo de gastos por cuenta
     * TTL: 5 minutos (configurado en application.yml)
     */
    @Cacheable(value = "expenseSummary", key = "#accountId")
    public ExpenseSummary calculateExpenseSummary(String accountId) {
        log.info("🔄 CACHE MISS - Calculating expense summary for account: {}", accountId);
        
        // Simular cálculo costoso
        simulateHeavyComputation();
        
        List<Expense> expenses = expenseService.getExpensesByAccount(accountId);
        
        if (expenses.isEmpty()) {
            return ExpenseSummary.builder()
                    .totalAmount(0.0)
                    .averageAmount(0.0)
                    .count(0)
                    .topMerchants(List.of())
                    .build();
        }
        
        double total = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
        
        double average = total / expenses.size();
        
        List<MerchantStats> topMerchants = calculateTopMerchants(accountId, 5);
        
        log.info("✅ CACHE STORED - Summary calculated: total={}, avg={}", total, average);
        
        return ExpenseSummary.builder()
                .totalAmount(total)
                .averageAmount(average)
                .count(expenses.size())
                .topMerchants(topMerchants)
                .build();
    }
    
    /**
     * RESOLVER-LEVEL CACHING por categoría
     */
    @Cacheable(value = "expenseSummaryByCategory", key = "#accountId + '_' + #category")
    public ExpenseSummary calculateExpenseSummaryByCategory(String accountId, Category category) {
        log.info("🔄 CACHE MISS - Calculating summary for {} / {}", accountId, category);
        
        simulateHeavyComputation();
        
        List<Expense> expenses = expenseService.getExpensesByCategory(accountId, category);
        
        if (expenses.isEmpty()) {
            return ExpenseSummary.builder()
                    .totalAmount(0.0)
                    .averageAmount(0.0)
                    .count(0)
                    .category(category)
                    .topMerchants(List.of())
                    .build();
        }
        
        double total = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
        
        double average = total / expenses.size();
        
        // Top merchants en esta categoría
        Map<String, MerchantStats> merchantMap = new HashMap<>();
        for (Expense expense : expenses) {
            merchantMap.merge(
                expense.getMerchantName(),
                MerchantStats.builder()
                    .merchantName(expense.getMerchantName())
                    .totalSpent(expense.getAmount())
                    .transactionCount(1)
                    .build(),
                (existing, newStat) -> {
                    existing.setTotalSpent(existing.getTotalSpent() + newStat.getTotalSpent());
                    existing.setTransactionCount(existing.getTransactionCount() + 1);
                    return existing;
                }
            );
        }
        
        List<MerchantStats> topMerchants = merchantMap.values().stream()
                .sorted((a, b) -> Double.compare(b.getTotalSpent(), a.getTotalSpent()))
                .limit(5)
                .collect(Collectors.toList());
        
        log.info("✅ CACHE STORED - Category summary: {}, total={}", category, total);
        
        return ExpenseSummary.builder()
                .totalAmount(total)
                .averageAmount(average)
                .count(expenses.size())
                .category(category)
                .topMerchants(topMerchants)
                .build();
    }
    
    /**
     * CONSULTA FRECUENTE - CACHEADA
     * Top merchants es una query muy común
     */
    @Cacheable(value = "topMerchants", key = "#accountId + '_' + #limit")
    public List<MerchantStats> calculateTopMerchants(String accountId, int limit) {
        log.info("🔄 CACHE MISS - Calculating top {} merchants for {}", limit, accountId);
        
        simulateHeavyComputation();
        
        List<Expense> expenses = expenseService.getExpensesByAccount(accountId);
        
        Map<String, MerchantStats> merchantMap = new HashMap<>();
        
        for (Expense expense : expenses) {
            merchantMap.merge(
                expense.getMerchantName(),
                MerchantStats.builder()
                    .merchantName(expense.getMerchantName())
                    .totalSpent(expense.getAmount())
                    .transactionCount(1)
                    .build(),
                (existing, newStat) -> {
                    existing.setTotalSpent(existing.getTotalSpent() + newStat.getTotalSpent());
                    existing.setTransactionCount(existing.getTransactionCount() + 1);
                    return existing;
                }
            );
        }
        
        List<MerchantStats> topMerchants = merchantMap.values().stream()
                .sorted((a, b) -> Double.compare(b.getTotalSpent(), a.getTotalSpent()))
                .limit(limit)
                .collect(Collectors.toList());
        
        log.info("✅ CACHE STORED - Top {} merchants calculated", limit);
        
        return topMerchants;
    }
    
    /**
     * CÁLCULO COSTOSO - ANÁLISIS MENSUAL
     * Este tipo de agregaciones se benefician enormemente de caching
     */
    @Cacheable(value = "monthlyAnalytics", key = "#accountId + '_' + #year + '_' + #month")
    public MonthlyAnalytics calculateMonthlyAnalytics(String accountId, int year, int month) {
        log.info("🔄 CACHE MISS - Calculating monthly analytics: {}/{} for {}", 
                year, month, accountId);
        
        // Simular cálculo MUY costoso
        simulateVeryHeavyComputation();
        
        List<Expense> expenses = expenseService.getExpensesByMonth(accountId, year, month);
        
        if (expenses.isEmpty()) {
            return MonthlyAnalytics.builder()
                    .month(String.format("%d-%02d", year, month))
                    .totalSpent(0.0)
                    .byCategory(List.of())
                    .build();
        }
        
        double totalSpent = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
        
        // Breakdown por categoría
        Map<Category, Double> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                    Expense::getCategory,
                    Collectors.summingDouble(Expense::getAmount)
                ));
        
        List<CategoryBreakdown> breakdown = categoryTotals.entrySet().stream()
                .map(entry -> CategoryBreakdown.builder()
                    .category(entry.getKey())
                    .amount(entry.getValue())
                    .percentage((entry.getValue() / totalSpent) * 100)
                    .build())
                .sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount()))
                .collect(Collectors.toList());
        
        // Gasto más alto del mes
        Expense topExpense = expenses.stream()
                .max(Comparator.comparing(Expense::getAmount))
                .orElse(null);
        
        log.info("✅ CACHE STORED - Monthly analytics: {}/{}, total={}", year, month, totalSpent);
        
        return MonthlyAnalytics.builder()
                .month(String.format("%d-%02d", year, month))
                .totalSpent(totalSpent)
                .byCategory(breakdown)
                .topExpense(topExpense)
                .build();
    }
    
    /**
     * Comparación entre meses
     * REUTILIZA el cache de monthlyAnalytics
     */
    public MonthComparison compareMonths(String accountId, int year1, int month1, 
                                        int year2, int month2) {
        log.info("Comparing months {}/{} vs {}/{} for {}", 
                year1, month1, year2, month2, accountId);
        
        // Estas llamadas usan el cache si está disponible
        MonthlyAnalytics m1 = calculateMonthlyAnalytics(accountId, year1, month1);
        MonthlyAnalytics m2 = calculateMonthlyAnalytics(accountId, year2, month2);
        
        double difference = m2.getTotalSpent() - m1.getTotalSpent();
        double percentageChange = m1.getTotalSpent() > 0 
            ? (difference / m1.getTotalSpent()) * 100 
            : 0;
        
        return MonthComparison.builder()
                .month1(m1)
                .month2(m2)
                .difference(difference)
                .percentageChange(percentageChange)
                .build();
    }
    
    /**
     * Invalidar cache cuando se crea un nuevo gasto
     */
    @CacheEvict(value = {
        "expenseSummary", 
        "expenseSummaryByCategory", 
        "topMerchants", 
        "monthlyAnalytics"
    }, key = "#accountId")
    public void invalidateCacheForAccount(String accountId) {
        log.warn("🗑️  CACHE INVALIDATED for account: {}", accountId);
    }
    
    /**
     * Simula un cálculo costoso (agregaciones complejas, validaciones, etc)
     */
    private void simulateHeavyComputation() {
        try {
            Thread.sleep(500); // 500ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Simula un cálculo MUY costoso
     */
    private void simulateVeryHeavyComputation() {
        try {
            Thread.sleep(1000); // 1 segundo delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
