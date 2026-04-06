package com.rajarata.banking.domain.fraud;

import com.rajarata.banking.domain.transactions.Transaction;
import java.time.LocalDateTime;
import java.util.UUID;

public class FraudAlert {
    private String alertId;
    private Transaction suspiciousTransaction;
    private String reason;
    private LocalDateTime timestamp;
    private String alertType; // "TRANSACTION" or "SECURITY"
    private String relatedUserId; // for security alerts (login attempts)

    // Constructor for transaction-based fraud alerts
    public FraudAlert(Transaction suspiciousTransaction, String reason) {
        this.alertId = UUID.randomUUID().toString();
        this.suspiciousTransaction = suspiciousTransaction;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
        this.alertType = "TRANSACTION";
    }

    // Constructor for security-based fraud alerts (e.g., failed logins)
    public FraudAlert(String userId, String reason, String alertType) {
        this.alertId = UUID.randomUUID().toString();
        this.suspiciousTransaction = null;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
        this.alertType = alertType;
        this.relatedUserId = userId;
    }

    public String getAlertId() { return alertId; }
    public Transaction getSuspiciousTransaction() { return suspiciousTransaction; }
    public String getReason() { return reason; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getAlertType() { return alertType; }
    public String getRelatedUserId() { return relatedUserId; }
}
