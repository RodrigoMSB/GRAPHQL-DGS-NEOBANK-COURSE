package com.neobank.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    
    private String id;
    private String accountId;
    private Double amount;
    private String currency;
    private String merchantName;
    private Category category;
    private LocalDate date;
    private String description;
}
