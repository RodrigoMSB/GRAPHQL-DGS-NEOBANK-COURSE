package com.neobank.fraud.resolver;

import com.neobank.fraud.model.FraudAlert;
import com.neobank.fraud.model.Transaction;
import com.neobank.fraud.publisher.FraudAlertPublisher;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsSubscription;
import com.netflix.graphql.dgs.InputArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@DgsComponent
public class FraudSubscriptionResolver {
    
    private static final Logger log = LoggerFactory.getLogger(FraudSubscriptionResolver.class);
    
    private final FraudAlertPublisher fraudAlertPublisher;
    
    public FraudSubscriptionResolver(FraudAlertPublisher fraudAlertPublisher) {
        this.fraudAlertPublisher = fraudAlertPublisher;
    }
    
    /**
     * Subscription para alertas de fraude en tiempo real
     * Se dispara cuando se detecta una transacción sospechosa
     */
    @DgsSubscription
    public Flux<FraudAlert> fraudAlertDetected(@InputArgument String accountId) {
        log.info("Client subscribed to fraud alerts for account: {}", accountId);
        return fraudAlertPublisher.getFraudAlertFlux(accountId);
    }
    
    /**
     * Subscription para cambios de estado de transacciones
     * Se dispara cuando una transacción cambia de PENDING -> APPROVED/FLAGGED
     */
    @DgsSubscription
    public Flux<Transaction> transactionStatusChanged(@InputArgument String accountId) {
        log.info("Client subscribed to transaction status changes for account: {}", accountId);
        return fraudAlertPublisher.getTransactionStatusFlux(accountId);
    }
}
