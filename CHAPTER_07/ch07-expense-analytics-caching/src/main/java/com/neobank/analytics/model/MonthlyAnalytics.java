package com.neobank.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyAnalytics {
    
    private String month;
    private Double totalSpent;
    private List<CategoryBreakdown> byCategory;
    private Expense topExpense;
}
