package com.neobank.fraud.publisher;

import com.neobank.fraud.model.FraudAlert;
import com.neobank.fraud.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class FraudAlertPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(FraudAlertPublisher.class);
    
    // Usar directBestEffort para que NO se complete automáticamente
    private final Sinks.Many<FraudAlert> fraudAlertSink = Sinks.many()
            .multicast()
            .directBestEffort();
    
    private final Sinks.Many<Transaction> transactionStatusSink = Sinks.many()
            .multicast()
            .directBestEffort();
    
    public void publishFraudAlert(FraudAlert alert) {
        log.info("📡 PUBLISHING fraud alert: {} for account: {}", 
                alert.getId(), alert.getTransaction().getAccountId());
        Sinks.EmitResult result = fraudAlertSink.tryEmitNext(alert);
        log.info("📡 Emit result: {}", result);
    }
    
    public void publishTransactionStatusChange(Transaction transaction) {
        log.info("📡 PUBLISHING transaction status change: {} - Status: {}", 
                transaction.getId(), transaction.getStatus());
        transactionStatusSink.tryEmitNext(transaction);
    }
    
    public Flux<FraudAlert> getFraudAlertFlux(String accountId) {
        log.info("🔔 New subscription for fraud alerts on account: {}", accountId);
        return fraudAlertSink.asFlux()
                .filter(alert -> alert.getTransaction().getAccountId().equals(accountId))
                .doOnSubscribe(s -> log.info("✅ Subscriber connected for account: {}", accountId))
                .doOnNext(alert -> log.info("📨 Sending alert to subscriber: {}", alert.getId()))
                .doOnCancel(() -> log.info("❌ Subscription cancelled for account: {}", accountId));
    }
    
    public Flux<Transaction> getTransactionStatusFlux(String accountId) {
        log.info("🔔 New subscription for transaction status on account: {}", accountId);
        return transactionStatusSink.asFlux()
                .filter(transaction -> transaction.getAccountId().equals(accountId))
                .doOnSubscribe(s -> log.info("✅ Subscriber connected for txn status: {}", accountId))
                .doOnCancel(() -> log.info("❌ Subscription cancelled for account: {}", accountId));
    }
}