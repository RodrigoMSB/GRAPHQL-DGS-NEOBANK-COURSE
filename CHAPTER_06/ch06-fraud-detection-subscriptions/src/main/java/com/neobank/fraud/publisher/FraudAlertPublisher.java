package com.neobank.fraud.publisher;

import com.neobank.fraud.model.FraudAlert;
import com.neobank.fraud.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Slf4j
@Component
public class FraudAlertPublisher {
    
    // Sink para FraudAlert subscriptions
    private final Sinks.Many<FraudAlert> fraudAlertSink = Sinks.many().multicast().onBackpressureBuffer();
    
    // Sink para Transaction status changes
    private final Sinks.Many<Transaction> transactionStatusSink = Sinks.many().multicast().onBackpressureBuffer();
    
    /**
     * Publica una alerta de fraude para que los suscriptores la reciban
     */
    public void publishFraudAlert(FraudAlert alert) {
        log.info("Publishing fraud alert: {} for account: {}", 
                alert.getId(), alert.getTransaction().getAccountId());
        fraudAlertSink.tryEmitNext(alert);
    }
    
    /**
     * Publica un cambio de estado de transacción
     */
    public void publishTransactionStatusChange(Transaction transaction) {
        log.info("Publishing transaction status change: {} - Status: {}", 
                transaction.getId(), transaction.getStatus());
        transactionStatusSink.tryEmitNext(transaction);
    }
    
    /**
     * Retorna un Flux filtrado por accountId para FraudAlerts
     */
    public Flux<FraudAlert> getFraudAlertFlux(String accountId) {
        log.info("New subscription created for fraud alerts on account: {}", accountId);
        return fraudAlertSink.asFlux()
                .filter(alert -> alert.getTransaction().getAccountId().equals(accountId))
                .doOnNext(alert -> log.debug("Emitting fraud alert to subscriber: {}", alert.getId()))
                .doOnCancel(() -> log.info("Subscription cancelled for account: {}", accountId));
    }
    
    /**
     * Retorna un Flux filtrado por accountId para Transaction status changes
     */
    public Flux<Transaction> getTransactionStatusFlux(String accountId) {
        log.info("New subscription created for transaction status on account: {}", accountId);
        return transactionStatusSink.asFlux()
                .filter(transaction -> transaction.getAccountId().equals(accountId))
                .doOnNext(txn -> log.debug("Emitting transaction status to subscriber: {}", txn.getId()))
                .doOnCancel(() -> log.info("Subscription cancelled for account: {}", accountId));
    }
}
