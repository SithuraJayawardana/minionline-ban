package com.rajarata.banking.domain.services;

import com.rajarata.banking.domain.loans.Loan;
import com.rajarata.banking.domain.loans.LoanType;

public class LoanService {

    // Returns interest rate based on loan type
    public double getInterestRate(LoanType type) {
        switch (type) {
            case PERSONAL: return 0.12; // 12%
            case MORTGAGE: return 0.06; // 6%
            case AUTO: return 0.08; // 8%
            case EDUCATION: return 0.04; // 4%
            default: return 0.10;
        }
    }

    // Apply for a new loan and save it
    public Loan applyForLoan(com.rajarata.banking.domain.users.Customer borrower, double amount, int termInMonths, LoanType type) {
        Loan loan = new Loan(borrower, amount, termInMonths, type);
        loan.setInterestRate(getInterestRate(type));

        // save loan in file storage
        com.rajarata.banking.db.FileStorageUtil.saveLoan(loan.getLoanId(), borrower.getUserId(), amount, termInMonths, type.name());
        return loan;
    }

    /**
     * Calculate monthly repayment using standard amortization formula
     * M = P [ r(1 + r)^n ] / [ (1 + r)^n - 1]
     */
    public double calculateMonthlyRepayment(Loan loan) {
        double principal = loan.getPrincipalAmount();
        double monthlyInterestRate = loan.getInterestRate() / 12.0;
        int numPayments = loan.getTermInMonths();
        
        if (monthlyInterestRate == 0) {
            return principal / numPayments;// zero interest loan

        }

        double compoundFactor = Math.pow(1.0 + monthlyInterestRate, numPayments);
        return (principal * monthlyInterestRate * compoundFactor) / (compoundFactor - 1.0);
    }

    // Apply late penalty to the remaining balance
    public void applyLatePenalty(Loan loan) {
        // Flat rate 500 LKR late penalty or 2% of remaining balance
        double penalty = 500.0;
        loan.setRemainingBalance(loan.getRemainingBalance() + penalty);
    }

     // Retrieve all loans for a given customer
    public java.util.List<Loan> getLoansForCustomer(com.rajarata.banking.domain.users.Customer customer) {
        java.util.List<Loan> customerLoans = new java.util.ArrayList<>();
        java.util.List<String> allLoansLines = com.rajarata.banking.db.FileStorageUtil.readAllLoans();
        for (String line : allLoansLines) {
            String[] parts = line.split(",");
            if (parts.length >= 6) {
                // New format: loanId, customerId, amount, term, type, date
                String loanId = parts[0];
                String customerId = parts[1];
                if (customerId.equals(customer.getUserId())) {
                    double amount = Double.parseDouble(parts[2]);
                    int term = Integer.parseInt(parts[3]);
                    LoanType type = LoanType.valueOf(parts[4]);
                    Loan loan = new Loan(customer, amount, term, type);
                    loan.setLoanId(loanId);
                    loan.setInterestRate(getInterestRate(type));
                    customerLoans.add(loan);
                }
            } else if (parts.length == 5) {
                // Old format: customerId, amount, term, type, date
                String customerId = parts[0];
                if (customerId.equals(customer.getUserId())) {
                    double amount = Double.parseDouble(parts[1]);
                    int term = Integer.parseInt(parts[2]);
                    LoanType type = LoanType.valueOf(parts[3]);
                    Loan loan = new Loan(customer, amount, term, type);
                    loan.setInterestRate(getInterestRate(type));
                    customerLoans.add(loan);
                }
            }
        }
        return customerLoans;
    }
}
