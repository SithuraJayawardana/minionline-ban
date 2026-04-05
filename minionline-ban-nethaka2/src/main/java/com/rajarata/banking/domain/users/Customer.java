package com.rajarata.banking.domain.users;

import java.time.LocalDate;

/**
 * Represents a banking customer. Inherits from User.
 */
public class Customer extends User {
    private String address;
    private LocalDate dateOfBirth;

    public Customer(String userId, String name, String email, String phoneNumber, String address, LocalDate dateOfBirth) {
        super(userId, name, email, phoneNumber);
        this.address = address;
        this.dateOfBirth = dateOfBirth;
    }

    // Getters and Setters
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}
