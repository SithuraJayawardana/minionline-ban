package com.rajarata.banking.domain.transactions;

import java.time.LocalDateTime;

/**
 * Represents a single banking transaction.
 */
public class Transaction {
    private String transactionId;
    private LocalDateTime timestamp;
    private TransactionType type;
    private double amount;
    private TransactionStatus status;

    // Constructor initializes a new transaction with PENDING status
    public Transaction(String transactionId, TransactionType type, double amount) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.status = TransactionStatus.PENDING; // Default status
    }

    // Getter and setter for transactionId
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    // Getter and setter for timestamp
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    // Getter and setter for transaction type
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    
    // Getter and setter for amount
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    // Getter and setter for transaction status
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
}
