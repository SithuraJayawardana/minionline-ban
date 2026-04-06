package com.rajarata.banking.domain.transactions;

/**
* Represents the current status of a banking transaction.
 */
public enum TransactionStatus {
    PENDING,// Transaction created but not yet processed
    SUCCESS,// Transaction completed successfully
    FAILED// Transaction failed
}
