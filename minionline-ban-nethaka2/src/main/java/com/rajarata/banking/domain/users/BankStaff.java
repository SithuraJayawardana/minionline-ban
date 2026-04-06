package com.rajarata.banking.domain.users;

import com.rajarata.banking.domain.loans.Loan;
import com.rajarata.banking.domain.loans.LoanStatus;

/**
 * Represents a regular staff member of the bank. Inherits from User.
 */
public class BankStaff extends User {
    private String employeeId;
    private String branchCode;

    public BankStaff(String userId, String name, String email, String phoneNumber, String employeeId, String branchCode) {
        super(userId, name, email, phoneNumber);
        this.employeeId = employeeId;
        this.branchCode = branchCode;
    }

    public void approveLoan(Loan loan) {
        if (loan != null && loan.getStatus() == LoanStatus.PENDING) {
            loan.setStatus(LoanStatus.APPROVED);
        } else {
            throw new IllegalStateException("Loan cannot be approved. It must exist and be in PENDING state.");
        }
    }

    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
}
