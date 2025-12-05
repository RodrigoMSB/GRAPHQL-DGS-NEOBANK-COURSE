package com.neobank.carbon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarbonAlert {
    
    private String id;
    private String transactionId;
    private AlertSeverity severity;
    private String message;
    private String recommendation;
    private LocalDateTime createdAt;
}
