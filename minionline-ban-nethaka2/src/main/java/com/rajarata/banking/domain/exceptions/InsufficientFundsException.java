package com.rajarata.banking.domain.exceptions;

/**
 * Exception thrown when an account does not have sufficient funds for a transaction.
 */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
