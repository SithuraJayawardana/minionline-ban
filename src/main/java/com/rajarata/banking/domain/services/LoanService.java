package com.rajarata.banking.domain.services;

import com.rajarata.banking.domain.loans.Loan;
import com.rajarata.banking.domain.loans.LoanType;

public class LoanService {
    
    public double getInterestRate(LoanType type) {
        switch (type) {
            case PERSONAL: return 0.12; // 12%
            case MORTGAGE: return 0.06; // 6%
            case AUTO: return 0.08; // 8%
            case EDUCATION: return 0.04; // 4%
            default: return 0.10;
        }
    }

    public Loan applyForLoan(com.rajarata.banking.domain.users.Customer borrower, double amount, int termInMonths, LoanType type) {
        Loan loan = new Loan(borrower, amount, termInMonths, type);
        loan.setInterestRate(getInterestRate(type));
        com.rajarata.banking.db.FileStorageUtil.saveLoan(borrower.getUserId(), amount, termInMonths, type.name());
        return loan;
    }

    /**
     * Standard amortization formula: M = P [ r(1 + r)^n ] / [ (1 + r)^n - 1]
     */
    public double calculateMonthlyRepayment(Loan loan) {
        double principal = loan.getPrincipalAmount();
        double monthlyInterestRate = loan.getInterestRate() / 12.0;
        int numPayments = loan.getTermInMonths();
        
        if (monthlyInterestRate == 0) {
            return principal / numPayments;
        }

        double compoundFactor = Math.pow(1.0 + monthlyInterestRate, numPayments);
        return (principal * monthlyInterestRate * compoundFactor) / (compoundFactor - 1.0);
    }

    public void applyLatePenalty(Loan loan) {
        // Flat rate 500 LKR late penalty or 2% of remaining balance
        double penalty = 500.0;
        loan.setRemainingBalance(loan.getRemainingBalance() + penalty);
    }
}
