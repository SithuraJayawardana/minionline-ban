package com.rajarata.banking.domain.services;

import com.rajarata.banking.domain.accounts.BankAccount;
import com.rajarata.banking.domain.transactions.Transaction;
import com.rajarata.banking.domain.transactions.TransactionStatus;
import com.rajarata.banking.domain.transactions.TransactionType;

import java.util.List;
import java.util.UUID;

public class SystemScheduler {

    // Process all accounts at the end of the month
    public void processEndOfMonth(List<BankAccount> allAccounts) {
        System.out.println("--- Starting End of Month Processing ---");
        for (BankAccount account : allAccounts) {
            // 1. Calculate interest (polymorphic, depends on account type)
            double interest = account.calculateInterest();
            if (interest > 0) {
                account.deposit(interest);// credit interest

                Transaction tx = new Transaction(UUID.randomUUID().toString(), TransactionType.INTEREST_CREDIT, interest);
                tx.setStatus(TransactionStatus.SUCCESS);
                account.addTransaction(tx);// record transaction
                System.out.println("Credited " + interest + " " + account.getCurrency() + " to account " + account.getAccountNumber());
            }

            // 2. Apply penalty if applicable (polymorphic)
            double penalty = account.applyPenalty();
            if (penalty > 0) {
                // penalty already deducted inside applyPenalty()
                // record penalty transaction
                Transaction tx = new Transaction(UUID.randomUUID().toString(), TransactionType.FEE_DEDUCTION, penalty);
                tx.setStatus(TransactionStatus.SUCCESS);
                account.addTransaction(tx);
                System.out.println("Deducted penalty " + penalty + " " + account.getCurrency() + " from account " + account.getAccountNumber());
            }
        }
        System.out.println("--- End of Month Processing Complete ---");
    }
}
