package com.neobank.fraud.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    private String id;
    private String accountId;
    private Double amount;
    private String currency;
    private String merchantName;
    private String category;
    private String location;
    private LocalDateTime timestamp;
    private Double riskScore;
    private TransactionStatus status;
    
    public enum TransactionStatus {
        PENDING,
        APPROVED,
        REJECTED,
        FLAGGED
    }
}
