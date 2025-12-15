package com.neobank.carbon.model;

import java.time.LocalDateTime;

public class CarbonAlert {
    
    private String id;
    private String transactionId;
    private AlertSeverity severity;
    private String message;
    private String recommendation;
    private LocalDateTime createdAt;
    
    public CarbonAlert() {}
    
    public CarbonAlert(String id, String transactionId, AlertSeverity severity,
                       String message, String recommendation, LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.severity = severity;
        this.message = message;
        this.recommendation = recommendation;
        this.createdAt = createdAt;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private String id, transactionId, message, recommendation;
        private AlertSeverity severity;
        private LocalDateTime createdAt;
        
        public Builder id(String id) { this.id = id; return this; }
        public Builder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public Builder severity(AlertSeverity severity) { this.severity = severity; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder recommendation(String recommendation) { this.recommendation = recommendation; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        
        public CarbonAlert build() {
            return new CarbonAlert(id, transactionId, severity, message, recommendation, createdAt);
        }
    }
}
