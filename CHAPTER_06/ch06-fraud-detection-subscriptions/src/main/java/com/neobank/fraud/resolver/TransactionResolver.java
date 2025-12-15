package com.neobank.fraud.resolver;

import com.neobank.fraud.model.FraudAlert;
import com.neobank.fraud.model.Transaction;
import com.neobank.fraud.publisher.FraudAlertPublisher;
import com.neobank.fraud.service.FraudDetectionService;
import com.neobank.fraud.service.TransactionService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DgsComponent
public class TransactionResolver {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionResolver.class);
    
    private final TransactionService transactionService;
    private final FraudDetectionService fraudDetectionService;
    private final FraudAlertPublisher fraudAlertPublisher;
    
    public TransactionResolver(TransactionService transactionService,
                               FraudDetectionService fraudDetectionService,
                               FraudAlertPublisher fraudAlertPublisher) {
        this.transactionService = transactionService;
        this.fraudDetectionService = fraudDetectionService;
        this.fraudAlertPublisher = fraudAlertPublisher;
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
    public List<FraudAlert> fraudAlerts(@InputArgument String accountId) {
        return fraudDetectionService.getFraudAlerts(accountId);
    }
    
    @DgsMutation
    public Map<String, Object> processTransaction(@InputArgument Map<String, Object> input) {
        log.info("Processing new transaction: {}", input);
        
        try {
            // Extraer datos del input
            String accountId = input.get("accountId").toString();
            Double amount = Double.parseDouble(input.get("amount").toString());
            String currency = input.get("currency").toString();
            String merchantName = input.get("merchantName").toString();
            String category = input.get("category").toString();
            String location = input.get("location").toString();
            
            // Crear transacción
            Transaction transaction = transactionService.createTransaction(
                accountId, amount, currency, merchantName, category, location
            );
            
            // Publicar cambio de estado (PENDING)
            fraudAlertPublisher.publishTransactionStatusChange(transaction);
            
            // Analizar fraude
            FraudAlert fraudAlert = fraudDetectionService.analyzeTransaction(transaction);
            
            // Si hay alerta de fraude, publicarla
            if (fraudAlert != null) {
                fraudAlertPublisher.publishFraudAlert(fraudAlert);
                
                // Publicar cambio de estado (FLAGGED)
                fraudAlertPublisher.publishTransactionStatusChange(transaction);
            } else {
                // Publicar cambio de estado (APPROVED)
                fraudAlertPublisher.publishTransactionStatusChange(transaction);
            }
            
            // Construir respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", fraudAlert != null ? 
                "Transaction flagged for potential fraud" : 
                "Transaction processed successfully");
            response.put("transaction", transaction);
            response.put("fraudAlert", fraudAlert);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error processing transaction", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("transaction", null);
            response.put("fraudAlert", null);
            return response;
        }
    }
}
