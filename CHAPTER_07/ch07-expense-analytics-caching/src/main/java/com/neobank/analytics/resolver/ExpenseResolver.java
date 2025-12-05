package com.neobank.analytics.resolver;

import com.neobank.analytics.dataloader.CategoryDataLoader;
import com.neobank.analytics.model.*;
import com.neobank.analytics.service.AnalyticsService;
import com.neobank.analytics.service.ExpenseService;
import com.netflix.graphql.dgs.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class ExpenseResolver {
    
    private final ExpenseService expenseService;
    private final AnalyticsService analyticsService;
    
    @DgsQuery
    public Expense expense(@InputArgument String id) {
        return expenseService.getExpenseById(id);
    }
    
    @DgsQuery
    public List<Expense> expenses(@InputArgument String accountId) {
        return expenseService.getExpensesByAccount(accountId);
    }
    
    /**
     * USA DATALOADER para per-request caching y batching
     */
    @DgsQuery
    public CompletableFuture<List<Expense>> expensesByCategory(
            @InputArgument String accountId,
            @InputArgument Category category,
            DgsDataFetchingEnvironment dfe) {
        
        log.info("Query: expensesByCategory - accountId={}, category={}", accountId, category);
        
        DataLoader<CategoryDataLoader.CategoryKey, List<Expense>> dataLoader = 
            dfe.getDataLoader("categoryExpenses");
        
        CategoryDataLoader.CategoryKey key = 
            new CategoryDataLoader.CategoryKey(accountId, category);
        
        return dataLoader.load(key);
    }
    
    /**
     * USA CACHE (@Cacheable en AnalyticsService)
     */
    @DgsQuery
    public ExpenseSummary expenseSummary(@InputArgument String accountId) {
        log.info("Query: expenseSummary - accountId={}", accountId);
        return analyticsService.calculateExpenseSummary(accountId);
    }
    
    /**
     * USA CACHE por categoría
     */
    @DgsQuery
    public ExpenseSummary expenseSummaryByCategory(
            @InputArgument String accountId,
            @InputArgument Category category) {
        
        log.info("Query: expenseSummaryByCategory - accountId={}, category={}", 
                accountId, category);
        
        return analyticsService.calculateExpenseSummaryByCategory(accountId, category);
    }
    
    @DgsQuery
    public List<MerchantStats> topMerchants(
            @InputArgument String accountId,
            @InputArgument Integer limit) {
        
        // Default value si no viene limit
        if (limit == null) {
            limit = 5;
        }
        
        log.info("Query: topMerchants - accountId={}, limit={}", accountId, limit);
        return analyticsService.calculateTopMerchants(accountId, limit);
    }
    
    /**
     * CÁLCULO COSTOSO - CACHEADO
     */
    @DgsQuery
    public MonthlyAnalytics monthlyAnalytics(
            @InputArgument String accountId,
            @InputArgument Integer year,
            @InputArgument Integer month) {
        
        log.info("Query: monthlyAnalytics - accountId={}, {}/{}", accountId, year, month);
        return analyticsService.calculateMonthlyAnalytics(accountId, year, month);
    }
    
    /**
     * REUTILIZA cache de monthlyAnalytics
     */
    @DgsQuery
    public MonthComparison compareMonths(
            @InputArgument String accountId,
            @InputArgument Integer year1,
            @InputArgument Integer month1,
            @InputArgument Integer year2,
            @InputArgument Integer month2) {
        
        log.info("Query: compareMonths - accountId={}, {}/{} vs {}/{}", 
                accountId, year1, month1, year2, month2);
        
        return analyticsService.compareMonths(accountId, year1, month1, year2, month2);
    }
    
    /**
     * MUTATION - Crear gasto e INVALIDAR cache
     */
    @DgsMutation
    public Map<String, Object> createExpense(@InputArgument Map<String, Object> input) {
        log.info("Mutation: createExpense - {}", input);
        
        try {
            String accountId = input.get("accountId").toString();
            Double amount = Double.parseDouble(input.get("amount").toString());
            String currency = input.get("currency").toString();
            String merchantName = input.get("merchantName").toString();
            Category category = Category.valueOf(input.get("category").toString());
            LocalDate date = LocalDate.parse(input.get("date").toString());
            String description = input.getOrDefault("description", "").toString();
            
            Expense expense = expenseService.createExpense(
                accountId, amount, currency, merchantName, category, date, description
            );
            
            // INVALIDAR CACHE
            analyticsService.invalidateCacheForAccount(accountId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expense created successfully");
            response.put("expense", expense);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error creating expense", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("expense", null);
            return response;
        }
    }
    
    /**
     * MUTATION - Invalidar cache manualmente
     */
    @DgsMutation
    public Boolean invalidateCache(@InputArgument String accountId) {
        log.info("Mutation: invalidateCache - accountId={}", accountId);
        analyticsService.invalidateCacheForAccount(accountId);
        return true;
    }
}
