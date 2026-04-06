package com.rajarata.banking.domain.services;

import com.rajarata.banking.domain.accounts.BankAccount;
import com.rajarata.banking.domain.fraud.FraudAlert;
import com.rajarata.banking.domain.transactions.Transaction;
import com.rajarata.banking.domain.users.Administrator;
import com.rajarata.banking.domain.users.User;
import com.rajarata.banking.domain.security.AuditLogger;
import java.time.LocalDateTime;
import java.util.List;

public class FraudDetectionService {

    // thresholds for detecting fraud
    private static final double LARGE_TRANSACTION_THRESHOLD = 500000.0;
    private static final int RAPID_TX_THRESHOLD = 3;
    private static final int RAPID_TX_SECONDS = 60;
    private static final int FAILED_LOGIN_THRESHOLD = 3; // alert on 3+ failed attempts
    
    private Administrator adminUser; // receives alerts
    private AuditLogger auditLogger;// logs fraud events

    public FraudDetectionService(Administrator adminUser, AuditLogger auditLogger) {
        this.adminUser = adminUser;
        this.auditLogger = auditLogger;
    }

    public void monitorTransaction(BankAccount account, Transaction currentTx) {
        // 1. Check for unusually large transaction
        if (currentTx.getAmount() > LARGE_TRANSACTION_THRESHOLD) {
            FraudAlert alert = new FraudAlert(currentTx, "Unusually large transaction amount: " + currentTx.getAmount() + " LKR");
            
            // send alert to admin and log it
            if (adminUser != null) adminUser.reviewFraudAlert(alert);
            if (auditLogger != null) auditLogger.logEvent("FRAUD_ALERT", "Large Tx: " + currentTx.getAmount() + " on account " + account.getAccountNumber());
        }

        // 2. Check for rapid multiple transactions
        List<Transaction> history = account.getTransactionHistory();
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusSeconds(RAPID_TX_SECONDS);
        
        long recentTxCount = history.stream()
                .filter(tx -> tx.getTimestamp() != null && tx.getTimestamp().isAfter(oneMinuteAgo))
                .count();

        // trigger alert if too many transactions happened quickly
        if (recentTxCount >= RAPID_TX_THRESHOLD) {
            FraudAlert alert = new FraudAlert(currentTx, "High transaction velocity: " + recentTxCount + " transactions in under a minute");
            if (adminUser != null) adminUser.reviewFraudAlert(alert);
            if (auditLogger != null) auditLogger.logEvent("FRAUD_ALERT", "Rapid Tx velocity on account " + account.getAccountNumber());
        }
    }

    /**
     * Monitor failed login attempts for security threats.
     * Alerts admin when threshold is exceeded.
     */
    public void monitorLoginAttempt(User user, boolean loginSuccess, int currentFailedAttempts) {
        // Only flag on failure (not success)
        if (!loginSuccess && currentFailedAttempts >= FAILED_LOGIN_THRESHOLD) {
            FraudAlert alert = new FraudAlert(
                user.getUserId(),
                "Repeated failed login attempts: " + currentFailedAttempts + " failed attempt(s) for user " + user.getUserId(),
                "SECURITY"
            );
            
            // send alert to admin and log it
            if (adminUser != null) adminUser.reviewFraudAlert(alert);
            if (auditLogger != null) auditLogger.logEvent(
                "FRAUD_ALERT_LOGIN",
                "Failed login threshold exceeded for user: " + user.getUserId() + " (" + currentFailedAttempts + " attempts)"
            );
        }
    }
}
