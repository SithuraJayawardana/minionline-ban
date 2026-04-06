package com.rajarata.banking.domain.security;

import com.rajarata.banking.domain.users.User;
import com.rajarata.banking.domain.exceptions.UnauthorizedAccessException;
import java.util.Base64;

public class AuthenticationService {
    private AuditLogger auditLogger;

    public AuthenticationService() {
        // create logger to record login activities
        this.auditLogger = new AuditLogger();
    }

    public String hashPassword(String rawPassword) {
        // convert password into a simple encoded form using Base64
        // this is just for learning, not secure for real systems
        return Base64.getEncoder().encodeToString(rawPassword.getBytes());
    }

    public boolean login(User user, String rawPassword) {
        // check if account is already locked
        if (user.isLocked()) {
            auditLogger.logEvent("LOGIN_FAILED", "Attempted login on locked account: " + user.getUserId());
            throw new UnauthorizedAccessException("Account is locked. Please contact support.");
        }

        // encode entered password and compare with saved password
        String hashedAttempt = hashPassword(rawPassword);
        if (hashedAttempt.equals(user.getPasswordHash())) {
            // correct password → login success
            user.setFailedLoginAttempts(0); // reset failed attempts
            return true;
        } else {
            // wrong password → increase failed attempts
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            
            if (attempts >= 3) {
                // lock account after 3 failed tries
                user.setLocked(true);
                auditLogger.logEvent("ACCOUNT_LOCKED", "User " + user.getUserId() + " locked out after 3 failed attempts.");
            } else {
                // log failed login attempt
                auditLogger.logEvent("LOGIN_FAILED", "Invalid credentials for user: " + user.getUserId());
            }
            return false;
        }
    }
}
