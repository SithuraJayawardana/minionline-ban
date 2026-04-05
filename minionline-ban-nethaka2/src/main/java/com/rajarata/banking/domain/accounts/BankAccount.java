package com.rajarata.banking.domain.accounts;

import com.rajarata.banking.domain.users.Customer;
import com.rajarata.banking.domain.rules.TransactionRules;
import com.rajarata.banking.domain.transactions.Transaction;
import com.rajarata.banking.domain.exceptions.InsufficientFundsException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all bank accounts.
 * Demonstrates composition by having a Customer as the owner.
 * Implements TransactionRules to enforce polymorphic behavior on all child accounts.
 */
public abstract class BankAccount implements TransactionRules {
    private String accountNumber;
    private double balance;
    private String currency; 
    private Customer owner; // Composition: Account HAS-A Customer
    private List<Transaction> transactionHistory; // History of all successful and failed operations

    public BankAccount(String accountNumber, Customer owner, double initialBalance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = initialBalance;
        this.currency = "LKR"; // Default currency requirement
        this.transactionHistory = new ArrayList<>();
    }

    // Common banking operations
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        this.balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid withdrawal amount.");
        }
        if (checkWithdrawalLimit(amount)) {
            this.balance -= amount;
        } else {
            throw new InsufficientFundsException("Insufficient funds or withdrawal limit exceeded.");
        }
    }

    public void addTransaction(Transaction transaction) {
        this.transactionHistory.add(transaction);
    }

    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory); // Return a copy for encapsulation
    }

    // Getters and Setters ensuring encapsulation
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public double getBalance() { return balance; }
    
    // Protected setter to allow derived classes to modify balance for fees/interest
    protected void setBalance(double balance) { this.balance = balance; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public Customer getOwner() { return owner; }
    public void setOwner(Customer owner) { this.owner = owner; }
}
