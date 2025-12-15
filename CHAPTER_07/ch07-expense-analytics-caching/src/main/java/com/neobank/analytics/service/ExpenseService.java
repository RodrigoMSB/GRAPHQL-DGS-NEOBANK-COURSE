package com.neobank.analytics.service;

import com.neobank.analytics.model.Category;
import com.neobank.analytics.model.Expense;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
    
    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);
    
    private final Map<String, Expense> expenses = new ConcurrentHashMap<>();
    private final Map<String, List<Expense>> expensesByAccount = new ConcurrentHashMap<>();
    
    public ExpenseService() {
        initializeSampleData();
    }
    
    public Expense createExpense(String accountId, Double amount, String currency,
                                 String merchantName, Category category, LocalDate date, String description) {
        
        String id = "exp-" + UUID.randomUUID().toString().substring(0, 8);
        
        Expense expense = Expense.builder()
                .id(id)
                .accountId(accountId)
                .amount(amount)
                .currency(currency)
                .merchantName(merchantName)
                .category(category)
                .date(date)
                .description(description)
                .build();
        
        expenses.put(id, expense);
        expensesByAccount.computeIfAbsent(accountId, k -> new ArrayList<>()).add(expense);
        
        log.info("Expense created: {} - {} {} at {}", id, amount, currency, merchantName);
        
        return expense;
    }
    
    public Expense getExpenseById(String id) {
        return expenses.get(id);
    }
    
    public List<Expense> getExpensesByAccount(String accountId) {
        return expensesByAccount.getOrDefault(accountId, new ArrayList<>());
    }
    
    public List<Expense> getExpensesByCategory(String accountId, Category category) {
        log.debug("Fetching expenses for account {} category {}", accountId, category);
        return getExpensesByAccount(accountId).stream()
                .filter(e -> e.getCategory() == category)
                .collect(Collectors.toList());
    }
    
    public List<Expense> getExpensesByMonth(String accountId, int year, int month) {
        return getExpensesByAccount(accountId).stream()
                .filter(e -> e.getDate().getYear() == year && e.getDate().getMonthValue() == month)
                .collect(Collectors.toList());
    }
    
    private void initializeSampleData() {
        // Account 001 - Noviembre 2024
        createExpense("account-001", 85.50, "USD", "Starbucks", Category.FOOD_DRINK, LocalDate.of(2024, 11, 1), "Coffee");
        createExpense("account-001", 125.00, "USD", "Whole Foods", Category.FOOD_DRINK, LocalDate.of(2024, 11, 2), "Groceries");
        createExpense("account-001", 45.20, "USD", "Uber", Category.TRANSPORTATION, LocalDate.of(2024, 11, 3), "Ride");
        createExpense("account-001", 200.00, "USD", "Amazon", Category.SHOPPING, LocalDate.of(2024, 11, 4), "Electronics");
        createExpense("account-001", 50.00, "USD", "Netflix", Category.ENTERTAINMENT, LocalDate.of(2024, 11, 5), "Subscription");
        createExpense("account-001", 150.00, "USD", "PG&E", Category.UTILITIES, LocalDate.of(2024, 11, 6), "Electricity");
        createExpense("account-001", 75.00, "USD", "CVS Pharmacy", Category.HEALTHCARE, LocalDate.of(2024, 11, 7), "Medicine");
        createExpense("account-001", 300.00, "USD", "United Airlines", Category.TRAVEL, LocalDate.of(2024, 11, 8), "Flight");
        createExpense("account-001", 60.00, "USD", "Starbucks", Category.FOOD_DRINK, LocalDate.of(2024, 11, 9), "Coffee");
        createExpense("account-001", 180.00, "USD", "Target", Category.SHOPPING, LocalDate.of(2024, 11, 10), "Household");
        
        // Account 001 - Diciembre 2024
        createExpense("account-001", 95.00, "USD", "Starbucks", Category.FOOD_DRINK, LocalDate.of(2024, 12, 1), "Coffee");
        createExpense("account-001", 140.00, "USD", "Whole Foods", Category.FOOD_DRINK, LocalDate.of(2024, 12, 2), "Groceries");
        createExpense("account-001", 55.00, "USD", "Lyft", Category.TRANSPORTATION, LocalDate.of(2024, 12, 3), "Ride");
        createExpense("account-001", 250.00, "USD", "Best Buy", Category.SHOPPING, LocalDate.of(2024, 12, 4), "Gadgets");
        createExpense("account-001", 50.00, "USD", "Spotify", Category.ENTERTAINMENT, LocalDate.of(2024, 12, 5), "Subscription");
        
        // Account 002 - Noviembre 2024
        createExpense("account-002", 120.00, "USD", "Chipotle", Category.FOOD_DRINK, LocalDate.of(2024, 11, 1), "Lunch");
        createExpense("account-002", 80.00, "USD", "Shell", Category.TRANSPORTATION, LocalDate.of(2024, 11, 2), "Gas");
        createExpense("account-002", 450.00, "USD", "Apple Store", Category.SHOPPING, LocalDate.of(2024, 11, 3), "iPhone");
        createExpense("account-002", 100.00, "USD", "AMC Theaters", Category.ENTERTAINMENT, LocalDate.of(2024, 11, 4), "Movies");
        createExpense("account-002", 200.00, "USD", "Comcast", Category.UTILITIES, LocalDate.of(2024, 11, 5), "Internet");
        
        log.info("Initialized {} sample expenses", expenses.size());
    }
}
