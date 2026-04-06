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
        double actualLimit = overdraftLimit;
        if ("USD".equalsIgnoreCase(getCurrency())) actualLimit = overdraftLimit / 320.0;
        else if ("EUR".equalsIgnoreCase(getCurrency())) actualLimit = overdraftLimit / 350.0;
        // Can withdraw up to the overdraft limit (meaning balance can become negative)
        return (getBalance() - amount) >= -actualLimit;
    }

    @Override
    public double applyPenalty() {
        // Apply penalty fee if the account is currently overdrawn
        if (getBalance() < 0) {
            double actualFee = OVERDRAFT_FEE;
            if ("USD".equalsIgnoreCase(getCurrency())) actualFee = OVERDRAFT_FEE / 320.0;
            else if ("EUR".equalsIgnoreCase(getCurrency())) actualFee = OVERDRAFT_FEE / 350.0;
            
            setBalance(getBalance() - actualFee);
            return actualFee;
        }
        return 0.0;
    }

    public double getOverdraftLimit() { return overdraftLimit; }
    public void setOverdraftLimit(double overdraftLimit) { this.overdraftLimit = overdraftLimit; }
}
