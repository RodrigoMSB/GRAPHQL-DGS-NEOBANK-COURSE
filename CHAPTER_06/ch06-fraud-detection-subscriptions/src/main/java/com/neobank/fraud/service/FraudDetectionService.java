package com.neobank.fraud.service;

import com.neobank.fraud.model.FraudAlert;
import com.neobank.fraud.model.RiskLevel;
import com.neobank.fraud.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {
    
    private final TransactionService transactionService;
    private final Map<String, List<FraudAlert>> alertsByAccount = new ConcurrentHashMap<>();
    
    // High-risk categories
    private static final List<String> HIGH_RISK_CATEGORIES = List.of(
        "Gambling", "Cryptocurrency", "Wire Transfer", "Cash Advance"
    );
    
    // Suspicious locations
    private static final List<String> SUSPICIOUS_LOCATIONS = List.of(
        "Nigeria", "Russia", "China", "North Korea"
    );
    
    /**
     * Analiza una transacción y detecta posibles fraudes
     * Retorna FraudAlert si se detecta algo sospechoso, null si todo está OK
     */
    public FraudAlert analyzeTransaction(Transaction transaction) {
        log.info("Analyzing transaction: {}", transaction.getId());
        
        List<String> reasons = new ArrayList<>();
        double riskScore = 0.0;
        
        // REGLA 1: Monto inusualmente alto (> 3x promedio)
        Double avgAmount = transactionService.getAverageTransactionAmount(transaction.getAccountId());
        if (transaction.getAmount() > avgAmount * 3) {
            reasons.add("Amount is 3x higher than account average ($" + 
                       String.format("%.2f", avgAmount) + ")");
            riskScore += 30.0;
        }
        
        // REGLA 2: Ubicación sospechosa
        for (String suspiciousLocation : SUSPICIOUS_LOCATIONS) {
            if (transaction.getLocation().contains(suspiciousLocation)) {
                reasons.add("Transaction from high-risk location: " + suspiciousLocation);
                riskScore += 40.0;
            }
        }
        
        // REGLA 3: Categoría de alto riesgo
        if (HIGH_RISK_CATEGORIES.contains(transaction.getCategory())) {
            reasons.add("High-risk category: " + transaction.getCategory());
            riskScore += 25.0;
        }
        
        // REGLA 4: Múltiples transacciones en poco tiempo (velocity check)
        List<Transaction> recentTransactions = transactionService.getRecentTransactions(
            transaction.getAccountId(), 5
        );
        if (recentTransactions.size() > 3) {
            reasons.add("Multiple transactions in last 5 minutes (" + recentTransactions.size() + ")");
            riskScore += 20.0;
        }
        
        // REGLA 5: Hora inusual (3 AM - 5 AM)
        LocalTime time = transaction.getTimestamp().toLocalTime();
        if (time.isAfter(LocalTime.of(3, 0)) && time.isBefore(LocalTime.of(5, 0))) {
            reasons.add("Unusual time: " + time);
            riskScore += 15.0;
        }
        
        // REGLA 6: Monto redondo sospechoso (múltiplo exacto de 1000)
        if (transaction.getAmount() % 1000 == 0 && transaction.getAmount() >= 5000) {
            reasons.add("Suspiciously round amount: $" + transaction.getAmount());
            riskScore += 10.0;
        }
        
        // Actualizar risk score de la transacción
        transactionService.updateTransactionRiskScore(transaction.getId(), riskScore);
        
        // Si detectamos algo sospechoso, crear alerta
        if (!reasons.isEmpty()) {
            RiskLevel riskLevel = determineRiskLevel(riskScore);
            
            // Actualizar estado de la transacción
            if (riskLevel == RiskLevel.CRITICAL || riskLevel == RiskLevel.HIGH) {
                transactionService.updateTransactionStatus(
                    transaction.getId(), 
                    Transaction.TransactionStatus.FLAGGED
                );
            }
            
            FraudAlert alert = FraudAlert.builder()
                    .id("alert-" + UUID.randomUUID().toString().substring(0, 8))
                    .transaction(transaction)
                    .riskLevel(riskLevel)
                    .reasons(reasons)
                    .detectedAt(LocalDateTime.now())
                    .recommendedAction(getRecommendedAction(riskLevel))
                    .build();
            
            // Guardar alerta
            alertsByAccount.computeIfAbsent(transaction.getAccountId(), k -> new ArrayList<>())
                    .add(alert);
            
            log.warn("FRAUD DETECTED: {} - Risk Score: {} - Reasons: {}", 
                    alert.getId(), riskScore, reasons);
            
            return alert;
        }
        
        // No se detectó fraude
        transactionService.updateTransactionStatus(
            transaction.getId(), 
            Transaction.TransactionStatus.APPROVED
        );
        
        log.info("Transaction {} approved - No fraud detected", transaction.getId());
        return null;
    }
    
    public List<FraudAlert> getFraudAlerts(String accountId) {
        return alertsByAccount.getOrDefault(accountId, new ArrayList<>());
    }
    
    private RiskLevel determineRiskLevel(double riskScore) {
        if (riskScore >= 80) return RiskLevel.CRITICAL;
        if (riskScore >= 50) return RiskLevel.HIGH;
        if (riskScore >= 25) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }
    
    private String getRecommendedAction(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case CRITICAL -> "BLOCK transaction immediately and contact customer";
            case HIGH -> "Require additional verification (2FA/OTP)";
            case MEDIUM -> "Flag for manual review within 24 hours";
            case LOW -> "Monitor closely for follow-up transactions";
        };
    }
}
