package com.rajarata.banking.domain.exceptions;

/**
 * Exception thrown when an account lookup operation fails.
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
