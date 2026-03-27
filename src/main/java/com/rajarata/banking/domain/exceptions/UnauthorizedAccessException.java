package com.rajarata.banking.domain.exceptions;

/**
 * Exception thrown when a user attempts an operation they are not authorized for.
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
