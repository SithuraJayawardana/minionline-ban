package com.rajarata.banking.domain.accounts;

import com.rajarata.banking.domain.users.Customer;

/**
 * Represents a Checking Account (Current Account).
 * Allows overdraft capabilities.
 */
public class CheckingAccount extends BankAccount {
    private double overdraftLimit;
    private static final double OVERDRAFT_FEE = 1500.0;

    public CheckingAccount(String accountNumber, Customer owner, double initialBalance, double overdraftLimit) {
        super(accountNumber, owner, initialBalance);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public double calculateInterest() {
        // Checking accounts typically don't earn interest
        return 0.0;
    }

    @Override
    public boolean checkWithdrawalLimit(double amount) {
        // Can withdraw up to the overdraft limit (meaning balance can become negative)
        return (getBalance() - amount) >= -overdraftLimit;
    }

    @Override
    public double applyPenalty() {
        // Apply penalty fee if the account is currently overdrawn
        if (getBalance() < 0) {
            setBalance(getBalance() - OVERDRAFT_FEE);
            return OVERDRAFT_FEE;
        }
        return 0.0;
    }

    public double getOverdraftLimit() { return overdraftLimit; }
    public void setOverdraftLimit(double overdraftLimit) { this.overdraftLimit = overdraftLimit; }
}
