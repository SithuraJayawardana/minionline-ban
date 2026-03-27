package com.rajarata.banking.domain.loans;

import com.rajarata.banking.domain.users.Customer;
import java.util.UUID;

public class Loan {
    private String loanId;
    private Customer borrower;
    private double principalAmount;
    private double remainingBalance;
    private int termInMonths;
    private double interestRate;
    private LoanType type;
    private LoanStatus status;

    public Loan(Customer borrower, double principalAmount, int termInMonths, LoanType type) {
        this.loanId = UUID.randomUUID().toString();
        this.borrower = borrower;
        this.principalAmount = principalAmount;
        this.remainingBalance = principalAmount;
        this.termInMonths = termInMonths;
        this.type = type;
        this.status = LoanStatus.PENDING;
        // Interest rate is set by LoanService later
    }

    // Getters and Setters ensuring Encapsulation
    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }

    public Customer getBorrower() { return borrower; }
    public void setBorrower(Customer borrower) { this.borrower = borrower; }

    public double getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(double principalAmount) { this.principalAmount = principalAmount; }

    public double getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(double remainingBalance) { this.remainingBalance = remainingBalance; }

    public int getTermInMonths() { return termInMonths; }
    public void setTermInMonths(int termInMonths) { this.termInMonths = termInMonths; }

    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }

    public LoanType getType() { return type; }
    public void setType(LoanType type) { this.type = type; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }
}
