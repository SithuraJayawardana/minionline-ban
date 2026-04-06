package com.rajarata.banking.domain.accounts;

import com.rajarata.banking.domain.users.Customer;

/**
 * Represents a Fixed Deposit Account.
 * Has early withdrawal penalties and a fixed term length.
 */
public class FixedDepositAccount extends BankAccount {
    private int termInMonths;
    private double fixedInterestRate;
    private boolean isMatured;

    public FixedDepositAccount(String accountNumber, Customer owner, double initialBalance, int termInMonths, double fixedInterestRate) {
        super(accountNumber, owner, initialBalance);
        this.termInMonths = termInMonths;
        this.fixedInterestRate = fixedInterestRate;
        this.isMatured = false; // Initially not matured
    }

    @Override
    public double calculateInterest() {
        // Simple interest calculation for the fixed term
        return getBalance() * fixedInterestRate * (termInMonths / 12.0);
    }

    @Override
    public boolean checkWithdrawalLimit(double amount) {
        // Typically, you can only withdraw the full balance (closing the FD)
        return amount <= getBalance();
    }

    @Override
    public double applyPenalty() {
        // Early withdrawal penalty if withdrawn prior to maturation
        if (!isMatured) {
            double penaltyPercentage = 0.02; // 2% penalty 
            double penaltyAmount = getBalance() * penaltyPercentage;
            setBalance(getBalance() - penaltyAmount);
            return penaltyAmount;
        }
        return 0.0;
    }

    // Getters and Setters
    public int getTermInMonths() { return termInMonths; }
    public void setTermInMonths(int termInMonths) { this.termInMonths = termInMonths; }
    
    public double getFixedInterestRate() { return fixedInterestRate; }
    public void setFixedInterestRate(double fixedInterestRate) { this.fixedInterestRate = fixedInterestRate; }
    
    public boolean isMatured() { return isMatured; }
    public void setMatured(boolean matured) { isMatured = matured; }
}
