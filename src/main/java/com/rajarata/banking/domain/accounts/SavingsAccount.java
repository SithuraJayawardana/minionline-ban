package com.rajarata.banking.domain.accounts;

import com.rajarata.banking.domain.users.Customer;

/**
 * Represents a Savings Account.
 */
public class SavingsAccount extends BankAccount {
    private static final double INTEREST_RATE = 0.04; // 4% annual interest
    private static final double MIN_BALANCE = 500.0; // Minimum LKR 500

    public SavingsAccount(String accountNumber, Customer owner, double initialBalance) {
        super(accountNumber, owner, initialBalance);
    }

    private double getActualMinBalance() {
        if ("USD".equalsIgnoreCase(getCurrency())) return MIN_BALANCE / 320.0;
        if ("EUR".equalsIgnoreCase(getCurrency())) return MIN_BALANCE / 350.0;
        return MIN_BALANCE;
    }

    @Override
    public double calculateInterest() {
        // Calculate interest based on current balance
        return getBalance() * INTEREST_RATE;
    }

    @Override
    public boolean checkWithdrawalLimit(double amount) {
        // Withdrawal must not cause the balance to drop below the minimum required balance
        return (getBalance() - amount) >= getActualMinBalance();
    }

    @Override
    public double applyPenalty() {
        // Apply penalty if balance falls below minimum despite constraints
        if (getBalance() < getActualMinBalance()) {
            double penalty = 50.0;
            if ("USD".equalsIgnoreCase(getCurrency())) penalty = 50.0 / 320.0;
            else if ("EUR".equalsIgnoreCase(getCurrency())) penalty = 50.0 / 350.0;
            
            setBalance(getBalance() - penalty);
            return penalty;
        }
        return 0.0;
    }
}
