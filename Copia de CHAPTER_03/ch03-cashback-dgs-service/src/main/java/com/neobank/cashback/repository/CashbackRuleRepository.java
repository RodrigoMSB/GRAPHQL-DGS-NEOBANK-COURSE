package com.neobank.cashback.repository;

import com.neobank.cashback.domain.CashbackRule;
import com.neobank.cashback.domain.TierMultipliers;
import com.neobank.cashback.domain.TransactionCategory;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repositorio de reglas de cashback.
 * 
 * Define cuánto cashback se otorga por cada categoría de transacción.
 */
@Repository
public class CashbackRuleRepository {
    
    private final Map<TransactionCategory, CashbackRule> rules = new ConcurrentHashMap<>();
    
    public CashbackRuleRepository() {
        initializeRules();
    }
    
    private void initializeRules() {
        // GROCERIES: 2% base, buen cashback
        rules.put(TransactionCategory.GROCERIES, new CashbackRule(
            "rule-001",
            TransactionCategory.GROCERIES,
            2.0,
            new TierMultipliers(1.0, 1.5, 2.0, 3.0),
            new BigDecimal("10.00"),
            new BigDecimal("50.00"),
            true
        ));
        
        // RESTAURANTS: 1.5% base
        rules.put(TransactionCategory.RESTAURANTS, new CashbackRule(
            "rule-002",
            TransactionCategory.RESTAURANTS,
            1.5,
            new TierMultipliers(1.0, 1.5, 2.0, 3.0),
            new BigDecimal("15.00"),
            new BigDecimal("40.00"),
            true
        ));
        
        // TRANSPORTATION: 1% base
        rules.put(TransactionCategory.TRANSPORTATION, new CashbackRule(
            "rule-003",
            TransactionCategory.TRANSPORTATION,
            1.0,
            new TierMultipliers(1.0, 1.5, 2.0, 3.0),
            new BigDecimal("5.00"),
            new BigDecimal("20.00"),
            true
        ));
        
        // ENTERTAINMENT: 1.2% base
        rules.put(TransactionCategory.ENTERTAINMENT, new CashbackRule(
            "rule-004",
            TransactionCategory.ENTERTAINMENT,
            1.2,
            new TierMultipliers(1.0, 1.5, 2.0, 3.0),
            new BigDecimal("10.00"),
            new BigDecimal("30.00"),
            true
        ));
        
        // SHOPPING: 1% base
        rules.put(TransactionCategory.SHOPPING, new CashbackRule(
            "rule-005",
            TransactionCategory.SHOPPING,
            1.0,
            new TierMultipliers(1.0, 1.5, 2.0, 3.0),
            new BigDecimal("20.00"),
            new BigDecimal("100.00"),
            true
        ));
        
        // HEALTH: 1.5% base
        rules.put(TransactionCategory.HEALTH, new CashbackRule(
            "rule-006",
            TransactionCategory.HEALTH,
            1.5,
            new TierMultipliers(1.0, 1.5, 2.0, 3.0),
            new BigDecimal("10.00"),
            new BigDecimal("60.00"),
            true
        ));
        
        // TRAVEL: 3% base, el más alto!
        rules.put(TransactionCategory.TRAVEL, new CashbackRule(
            "rule-007",
            TransactionCategory.TRAVEL,
            3.0,
            new TierMultipliers(1.0, 1.5, 2.0, 3.0),
            new BigDecimal("50.00"),
            new BigDecimal("200.00"),
            true
        ));
        
        // UTILITIES: 0.5% base, el más bajo
        rules.put(TransactionCategory.UTILITIES, new CashbackRule(
            "rule-008",
            TransactionCategory.UTILITIES,
            0.5,
            new TierMultipliers(1.0, 1.5, 2.0, 3.0),
            new BigDecimal("10.00"),
            new BigDecimal("15.00"),
            true
        ));
        
        // EDUCATION: 2% base
        rules.put(TransactionCategory.EDUCATION, new CashbackRule(
            "rule-009",
            TransactionCategory.EDUCATION,
            2.0,
            new TierMultipliers(1.0, 1.5, 2.0, 3.0),
            new BigDecimal("20.00"),
            new BigDecimal("80.00"),
            true
        ));
        
        // OTHER: 0.5% base
        rules.put(TransactionCategory.OTHER, new CashbackRule(
            "rule-010",
            TransactionCategory.OTHER,
            0.5,
            new TierMultipliers(1.0, 1.5, 2.0, 3.0),
            new BigDecimal("10.00"),
            new BigDecimal("10.00"),
            true
        ));
    }
    
    public Optional<CashbackRule> findByCategory(TransactionCategory category) {
        return Optional.ofNullable(rules.get(category));
    }
    
    public List<CashbackRule> findAll() {
        return new ArrayList<>(rules.values());
    }
    
    public List<CashbackRule> findActive() {
        return rules.values().stream()
            .filter(CashbackRule::getIsActive)
            .toList();
    }
}
