package com.rajarata.banking.domain.users;

/**
 * Abstract base class for all users in the Rajarata Digital Banking Application.
 * Demonstrates abstraction and encapsulation.
 */
public abstract class User {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    
    // Security fields
    private String passwordHash;
    private int failedLoginAttempts;
    private boolean isLocked;

    public User(String userId, String name, String email, String phoneNumber) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.failedLoginAttempts = 0;
        this.isLocked = false;
    }

    // Getters and Setters ensuring encapsulation
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean isLocked) { this.isLocked = isLocked; }
}
