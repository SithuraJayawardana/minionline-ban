package com.rajarata.banking.domain.exceptions;

/**
 * Exception thrown when a transaction exceeds the daily allowed limit for an account.
 */
public class DailyLimitExceededException extends RuntimeException {
    public DailyLimitExceededException(String message) {
        super(message);
    }
}
