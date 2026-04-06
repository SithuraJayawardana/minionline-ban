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

    @Override
    public double calculateInterest() {
        // Calculate interest based on current balance
        return getBalance() * INTEREST_RATE;
    }

    @Override
    public boolean checkWithdrawalLimit(double amount) {
        // Withdrawal must not cause the balance to drop below the minimum required balance
        return (getBalance() - amount) >= MIN_BALANCE;
    }

    @Override
    public double applyPenalty() {
        // Apply penalty if balance falls below minimum despite constraints
        if (getBalance() < MIN_BALANCE) {
            double penalty = 50.0;
            setBalance(getBalance() - penalty);
            return penalty;
        }
        return 0.0;
    }
}
