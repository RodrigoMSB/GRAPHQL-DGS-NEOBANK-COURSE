package com.neobank.analytics.dataloader;

import com.neobank.analytics.model.Category;
import com.neobank.analytics.model.Expense;
import com.neobank.analytics.service.ExpenseService;
import com.netflix.graphql.dgs.DgsDataLoader;
import org.dataloader.BatchLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * PER-REQUEST CACHING con DataLoader
 * 
 * Problema N+1:
 * Sin DataLoader, si una query pide 10 categorías, se harían 10 llamadas individuales a la DB.
 * 
 * Con DataLoader:
 * - Batching: Agrupa múltiples requests en uno solo
 * - Caching: Durante la misma petición HTTP, si se pide la misma categoría 2 veces, usa cache
 * 
 * Ejemplo:
 * Query {
 *   expensesByCategory(category: FOOD_DRINK)    ← Request 1
 *   expensesByCategory(category: SHOPPING)      ← Request 2
 *   expensesByCategory(category: FOOD_DRINK)    ← CACHE HIT (no DB call)
 * }
 * 
 * DataLoader batchea requests 1 y 2 en una sola llamada, y request 3 usa cache.
 */
@DgsDataLoader(name = "categoryExpenses")
public class CategoryDataLoader implements BatchLoader<CategoryDataLoader.CategoryKey, List<Expense>> {
    
    private static final Logger log = LoggerFactory.getLogger(CategoryDataLoader.class);
    
    private final ExpenseService expenseService;
    
    public CategoryDataLoader(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }
    
    /**
     * Key compuesta para DataLoader
     * Necesitamos accountId + category para identificar uniquely cada request
     */
    public static class CategoryKey {
        private String accountId;
        private Category category;
        
        public CategoryKey() {
        }
        
        public CategoryKey(String accountId, Category category) {
            this.accountId = accountId;
            this.category = category;
        }
        
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        
        public Category getCategory() { return category; }
        public void setCategory(Category category) { this.category = category; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CategoryKey that = (CategoryKey) o;
            return Objects.equals(accountId, that.accountId) && category == that.category;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(accountId, category);
        }
        
        @Override
        public String toString() {
            return "CategoryKey{accountId='" + accountId + "', category=" + category + "}";
        }
    }
    
    @Override
    public CompletionStage<List<List<Expense>>> load(List<CategoryKey> keys) {
        log.info("📦 DATALOADER BATCH - Loading {} categories", keys.size());
        log.debug("Keys: {}", keys);
        
        return CompletableFuture.supplyAsync(() -> {
            List<List<Expense>> result = new ArrayList<>();
            
            // Procesar cada key del batch
            for (CategoryKey key : keys) {
                log.debug("  - Fetching: accountId={}, category={}", 
                         key.getAccountId(), key.getCategory());
                
                List<Expense> expenses = expenseService.getExpensesByCategory(
                    key.getAccountId(), 
                    key.getCategory()
                );
                
                result.add(expenses);
            }
            
            log.info("✅ DATALOADER BATCH COMPLETE - Returned {} result sets", result.size());
            
            return result;
        });
    }
}
