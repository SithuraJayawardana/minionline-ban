package com.rajarata.banking.domain.services;

import com.rajarata.banking.domain.accounts.BankAccount;
import com.rajarata.banking.domain.fraud.FraudAlert;
import com.rajarata.banking.domain.transactions.Transaction;
import com.rajarata.banking.domain.users.Administrator;
import com.rajarata.banking.domain.security.AuditLogger;
import java.time.LocalDateTime;
import java.util.List;

public class FraudDetectionService {
    private static final double LARGE_TRANSACTION_THRESHOLD = 500000.0;
    private static final int RAPID_TX_THRESHOLD = 3;
    private static final int RAPID_TX_SECONDS = 60;
    
    private Administrator adminUser; // Simulated routing target for alerts
    private AuditLogger auditLogger;

    public FraudDetectionService(Administrator adminUser, AuditLogger auditLogger) {
        this.adminUser = adminUser;
        this.auditLogger = auditLogger;
    }

    public void monitorTransaction(BankAccount account, Transaction currentTx) {
        // 1. Unusually large transaction amount flag
        if (currentTx.getAmount() > LARGE_TRANSACTION_THRESHOLD) {
            FraudAlert alert = new FraudAlert(currentTx, "Unusually large transaction amount: " + currentTx.getAmount() + " LKR");
            if (adminUser != null) adminUser.reviewFraudAlert(alert);
            if (auditLogger != null) auditLogger.logEvent("FRAUD_ALERT", "Large Tx: " + currentTx.getAmount() + " on account " + account.getAccountNumber());
        }

        // 2. Rapid transaction velocity flag
        List<Transaction> history = account.getTransactionHistory();
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusSeconds(RAPID_TX_SECONDS);
        
        long recentTxCount = history.stream()
                .filter(tx -> tx.getTimestamp() != null && tx.getTimestamp().isAfter(oneMinuteAgo))
                .count();

        // Note: history might already include currentTx if the caller appends first
        if (recentTxCount >= RAPID_TX_THRESHOLD) {
            FraudAlert alert = new FraudAlert(currentTx, "High transaction velocity: " + recentTxCount + " transactions in under a minute");
            if (adminUser != null) adminUser.reviewFraudAlert(alert);
            if (auditLogger != null) auditLogger.logEvent("FRAUD_ALERT", "Rapid Tx velocity on account " + account.getAccountNumber());
        }
    }
}
