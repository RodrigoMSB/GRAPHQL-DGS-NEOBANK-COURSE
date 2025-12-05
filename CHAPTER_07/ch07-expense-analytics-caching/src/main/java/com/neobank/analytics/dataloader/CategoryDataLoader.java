package com.neobank.analytics.dataloader;

import com.neobank.analytics.model.Category;
import com.neobank.analytics.model.Expense;
import com.neobank.analytics.service.ExpenseService;
import com.netflix.graphql.dgs.DgsDataLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.BatchLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

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
@Slf4j
@DgsDataLoader(name = "categoryExpenses")
@RequiredArgsConstructor
public class CategoryDataLoader implements BatchLoader<CategoryDataLoader.CategoryKey, List<Expense>> {
    
    private final ExpenseService expenseService;
    
    /**
     * Key compuesta para DataLoader
     * Necesitamos accountId + category para identificar uniquely cada request
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CategoryKey {
        private String accountId;
        private Category category;
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
