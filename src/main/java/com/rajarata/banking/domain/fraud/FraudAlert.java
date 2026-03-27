package com.rajarata.banking.domain.fraud;

import com.rajarata.banking.domain.transactions.Transaction;
import java.time.LocalDateTime;
import java.util.UUID;

public class FraudAlert {
    private String alertId;
    private Transaction suspiciousTransaction;
    private String reason;
    private LocalDateTime timestamp;

    public FraudAlert(Transaction suspiciousTransaction, String reason) {
        this.alertId = UUID.randomUUID().toString();
        this.suspiciousTransaction = suspiciousTransaction;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    public String getAlertId() { return alertId; }
    public Transaction getSuspiciousTransaction() { return suspiciousTransaction; }
    public String getReason() { return reason; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
