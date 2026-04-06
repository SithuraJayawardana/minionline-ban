package com.rajarata.banking.domain.accounts;

import com.rajarata.banking.domain.users.Customer;
import com.rajarata.banking.domain.exceptions.DailyLimitExceededException;

/**
 * Represents a Student Account.
 * Has strict low limit constraints but no strict minimum balance penalties.
 */
public class StudentAccount extends BankAccount {
    private static final double DAILY_WITHDRAWAL_LIMIT = 5000.0; // Max LKR 5000 per transaction
    private static final double INTEREST_RATE = 0.05; // 5% interest (higher for students)
    
    public StudentAccount(String accountNumber, Customer owner, double initialBalance) {
        super(accountNumber, owner, initialBalance);
    }

    @Override
    public double calculateInterest() {
        return getBalance() * INTEREST_RATE;
    }

    @Override
    public boolean checkWithdrawalLimit(double amount) {
        // Cannot overdraw at all, and cannot exceed daily limit
        return amount <= DAILY_WITHDRAWAL_LIMIT && (getBalance() - amount) >= 0;
    }

    @Override
    public void withdraw(double amount) {
        if (amount > DAILY_WITHDRAWAL_LIMIT) {
            throw new DailyLimitExceededException("Withdrawal exceeds daily limit of " + DAILY_WITHDRAWAL_LIMIT);
        }
        super.withdraw(amount);
    }

    @Override
    public double applyPenalty() {
        // Student accounts have no minimum balance penalties
        return 0.0;
    }
}
