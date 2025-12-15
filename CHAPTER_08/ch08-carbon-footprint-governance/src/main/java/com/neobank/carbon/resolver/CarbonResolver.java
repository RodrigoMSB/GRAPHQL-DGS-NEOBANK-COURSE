package com.neobank.carbon.resolver;

import com.neobank.carbon.governance.SchemaVersionService;
import com.neobank.carbon.model.*;
import com.neobank.carbon.service.*;
import com.netflix.graphql.dgs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@DgsComponent
public class CarbonResolver {
    
    private static final Logger log = LoggerFactory.getLogger(CarbonResolver.class);
    
    private final TransactionService transactionService;
    private final CarbonCalculatorService carbonCalculator;
    private final ESGService esgService;
    private final SchemaVersionService schemaVersionService;
    
    private final Map<String, List<CarbonAlert>> alertsByAccount = new HashMap<>();
    
    public CarbonResolver(TransactionService transactionService,
                         CarbonCalculatorService carbonCalculator,
                         ESGService esgService,
                         SchemaVersionService schemaVersionService) {
        this.transactionService = transactionService;
        this.carbonCalculator = carbonCalculator;
        this.esgService = esgService;
        this.schemaVersionService = schemaVersionService;
    }
    
    @DgsQuery
    public Transaction transaction(@InputArgument String id) {
        return transactionService.getTransactionById(id);
    }
    
    @DgsQuery
    public List<Transaction> transactions(@InputArgument String accountId) {
        return transactionService.getTransactionsByAccount(accountId);
    }
    
    @DgsQuery
    public List<Transaction> transactionsByImpact(
            @InputArgument String accountId,
            @InputArgument ImpactLevel impactLevel) {
        return transactionService.getTransactionsByImpact(accountId, impactLevel);
    }
    
    @DgsQuery
    public SustainabilityReport sustainabilityReport(
            @InputArgument String accountId,
            @InputArgument Integer year,
            @InputArgument Integer month) {
        
        List<Transaction> transactions = transactionService.getTransactionsByMonth(accountId, year, month);
        
        if (transactions.isEmpty()) {
            return SustainabilityReport.builder()
                    .accountId(accountId)
                    .period(String.format("%d-%02d", year, month))
                    .totalCO2Kg(0.0)
                    .totalTransactions(0)
                    .recommendations(List.of("Start tracking your carbon footprint!"))
                    .build();
        }
        
        double totalCO2 = transactions.stream()
                .mapToDouble(t -> t.getCarbonFootprint().getCo2Kg())
                .sum();
        
        double avgCO2 = totalCO2 / transactions.size();
        
        long offsetCount = transactions.stream()
                .filter(t -> t.getCarbonFootprint().getOffsetPurchased())
                .count();
        
        double offsetPercentage = (offsetCount * 100.0) / transactions.size();
        
        double avgESG = transactions.stream()
                .filter(t -> t.getEsgScore() != null)
                .mapToDouble(t -> t.getEsgScore().getOverall())
                .average()
                .orElse(0.0);
        
        Transaction highestImpact = transactions.stream()
                .max(Comparator.comparing(t -> t.getCarbonFootprint().getCo2Kg()))
                .orElse(null);
        
        List<String> recommendations = generateRecommendations(totalCO2, offsetPercentage, avgESG);
        
        return SustainabilityReport.builder()
                .accountId(accountId)
                .period(String.format("%d-%02d", year, month))
                .totalCO2Kg(totalCO2)
                .totalTransactions(transactions.size())
                .averageCO2PerTransaction(avgCO2)
                .highestImpactTransaction(highestImpact)
                .offsetPercentage(offsetPercentage)
                .esgAverageScore(avgESG)
                .recommendations(recommendations)
                .build();
    }
    
    @DgsQuery
    public PeriodComparison comparePeriods(
            @InputArgument String accountId,
            @InputArgument Integer year1,
            @InputArgument Integer month1,
            @InputArgument Integer year2,
            @InputArgument Integer month2) {
        
        SustainabilityReport report1 = sustainabilityReport(accountId, year1, month1);
        SustainabilityReport report2 = sustainabilityReport(accountId, year2, month2);
        
        double change = report2.getTotalCO2Kg() - report1.getTotalCO2Kg();
        double percentageChange = report1.getTotalCO2Kg() > 0 
                ? (change / report1.getTotalCO2Kg()) * 100 
                : 0;
        
        Trend trend;
        if (Math.abs(percentageChange) < 5) {
            trend = Trend.STABLE;
        } else if (percentageChange < 0) {
            trend = Trend.IMPROVING;
        } else {
            trend = Trend.WORSENING;
        }
        
        return PeriodComparison.builder()
                .previousPeriod(report1.getPeriod())
                .co2Change(change)
                .percentageChange(percentageChange)
                .trend(trend)
                .build();
    }
    
    @DgsQuery
    public List<CarbonAlert> carbonAlerts(@InputArgument String accountId) {
        return alertsByAccount.getOrDefault(accountId, new ArrayList<>());
    }
    
    @DgsQuery
    public ESGScore merchantESG(@InputArgument String merchantName) {
        return esgService.getESGScore(merchantName, MerchantCategory.OTHER);
    }
    
    @DgsQuery
    public Map<String, Object> schemaVersion() {
        return schemaVersionService.getVersionInfo();
    }
    
    @DgsMutation
    public Map<String, Object> createTransaction(@InputArgument Map<String, Object> input) {
        log.info("Mutation: createTransaction");
        
        try {
            String accountId = input.get("accountId").toString();
            Double amount = Double.parseDouble(input.get("amount").toString());
            String currency = input.get("currency").toString();
            String merchantName = input.get("merchantName").toString();
            MerchantCategory merchantCategory = MerchantCategory.valueOf(
                    input.get("merchantCategory").toString());
            LocalDate date = LocalDate.parse(input.get("date").toString());
            
            Transaction transaction = transactionService.createTransaction(
                    accountId, amount, currency, merchantName, merchantCategory, date);
            
            CarbonAlert alert = null;
            if (transaction.getCarbonFootprint().getImpactLevel() == ImpactLevel.CRITICAL) {
                alert = createCarbonAlert(transaction);
                alertsByAccount.computeIfAbsent(accountId, k -> new ArrayList<>()).add(alert);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Transaction created successfully");
            response.put("transaction", transaction);
            response.put("carbonAlert", alert);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error creating transaction", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return response;
        }
    }
    
    @DgsMutation
    public Map<String, Object> purchaseCarbonOffset(@InputArgument String transactionId) {
        log.info("Mutation: purchaseCarbonOffset");
        
        boolean success = transactionService.purchaseOffset(transactionId);
        
        if (success) {
            Transaction txn = transactionService.getTransactionById(transactionId);
            CarbonFootprint footprint = txn.getCarbonFootprint();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Carbon offset purchased successfully");
            response.put("offsetCost", footprint.getOffsetCost());
            response.put("co2Offset", footprint.getCo2Kg());
            response.put("certificateId", "CERT-" + UUID.randomUUID().toString().substring(0, 8));
            
            return response;
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Transaction not found or already offset");
        return response;
    }
    
    @Deprecated
    @DgsMutation
    public Boolean buyOffset(@InputArgument String transactionId) {
        log.warn("⚠️ DEPRECATED ENDPOINT USED: buyOffset - Use purchaseCarbonOffset instead");
        return transactionService.purchaseOffset(transactionId);
    }
    
    private CarbonAlert createCarbonAlert(Transaction transaction) {
        String alertId = "alert-" + UUID.randomUUID().toString().substring(0, 8);
        
        return CarbonAlert.builder()
                .id(alertId)
                .transactionId(transaction.getId())
                .severity(AlertSeverity.CRITICAL)
                .message(String.format("High carbon footprint detected: %.2f kg CO2", 
                        transaction.getCarbonFootprint().getCo2Kg()))
                .recommendation("Consider purchasing carbon offset or choosing sustainable alternatives")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    private List<String> generateRecommendations(double totalCO2, double offsetPercentage, double avgESG) {
        List<String> recommendations = new ArrayList<>();
        
        if (totalCO2 > 100) {
            recommendations.add("Consider reducing air travel");
        }
        if (offsetPercentage < 20) {
            recommendations.add("Purchase carbon offsets for high-impact transactions");
        }
        if (avgESG < 60) {
            recommendations.add("Choose merchants with better ESG ratings");
        }
        recommendations.add("Track your progress monthly");
        
        return recommendations;
    }
}
