package com.rajarata.banking.domain.users;

import com.rajarata.banking.domain.fraud.FraudAlert;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a system administrator with elevated privileges.
 * Inherits from User.. Inherits from User.
 */
public class Administrator extends User {
    private String adminLevel; // e.g., "SuperAdmin", "SystemAdmin"
    private String department;
    private List<FraudAlert> fraudAlerts;

    // Constructor initializes admin details and empty fraud alert list
    public Administrator(String userId, String name, String email, String phoneNumber, String adminLevel, String department) {
        super(userId, name, email, phoneNumber);
        this.adminLevel = adminLevel;
        this.department = department;
        this.fraudAlerts = new ArrayList<>();
    }

    // Receive and review a fraud alert
    public void reviewFraudAlert(FraudAlert alert) {
        if (alert != null) {
            fraudAlerts.add(alert);
            System.out.println("Administrator " + getName() + " received Fraud Alert: " + alert.getReason() + " for TxId: " + alert.getSuspiciousTransaction().getTransactionId());
        }
    }

    // Return a copy of fraud alerts to prevent external modification
    public List<FraudAlert> getFraudAlerts() {
        return new ArrayList<>(fraudAlerts); // defensive copy
    }

    // Getters and Setters
    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
