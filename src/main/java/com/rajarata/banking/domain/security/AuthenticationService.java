package com.rajarata.banking.domain.security;

import com.rajarata.banking.domain.users.User;
import com.rajarata.banking.domain.exceptions.UnauthorizedAccessException;
import java.util.Base64;

public class AuthenticationService {
    private AuditLogger auditLogger;

    public AuthenticationService() {
        this.auditLogger = new AuditLogger();
    }

    public String hashPassword(String rawPassword) {
        // Mocking a hashing concept with Base64 encoding. 
        // In real-world projects, use algorithms like BCrypt or Argon2.
        return Base64.getEncoder().encodeToString(rawPassword.getBytes());
    }

    public boolean login(User user, String rawPassword) {
        if (user.isLocked()) {
            auditLogger.logEvent("LOGIN_FAILED", "Attempted login on locked account: " + user.getUserId());
            throw new UnauthorizedAccessException("Account is locked. Please contact support.");
        }

        String hashedAttempt = hashPassword(rawPassword);
        if (hashedAttempt.equals(user.getPasswordHash())) {
            // Success
            user.setFailedLoginAttempts(0); // Reset attempts
            return true;
        } else {
            // Failure
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            
            if (attempts >= 3) {
                user.setLocked(true);
                auditLogger.logEvent("ACCOUNT_LOCKED", "User " + user.getUserId() + " locked out after 3 failed attempts.");
            } else {
                auditLogger.logEvent("LOGIN_FAILED", "Invalid credentials for user: " + user.getUserId());
            }
            return false;
        }
    }
}
