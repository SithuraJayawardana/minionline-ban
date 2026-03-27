package com.rajarata.banking.domain.services;

import com.rajarata.banking.domain.accounts.BankAccount;
import com.rajarata.banking.domain.transactions.Transaction;
import com.rajarata.banking.domain.transactions.TransactionStatus;
import com.rajarata.banking.domain.transactions.TransactionType;

import java.util.List;
import java.util.UUID;

public class SystemScheduler {

    public void processEndOfMonth(List<BankAccount> allAccounts) {
        System.out.println("--- Starting End of Month Processing ---");
        for (BankAccount account : allAccounts) {
            // Polymorphic Interest Calculation
            double interest = account.calculateInterest();
            if (interest > 0) {
                account.deposit(interest);
                Transaction tx = new Transaction(UUID.randomUUID().toString(), TransactionType.INTEREST_CREDIT, interest);
                tx.setStatus(TransactionStatus.SUCCESS);
                account.addTransaction(tx);
                System.out.println("Credited " + interest + " " + account.getCurrency() + " to account " + account.getAccountNumber());
            }

            // Polymorphic Penalty Application
            double penalty = account.applyPenalty();
            if (penalty > 0) {
                // penalty is already applied inside the method via protected setBalance
                // but we need to record it
                Transaction tx = new Transaction(UUID.randomUUID().toString(), TransactionType.FEE_DEDUCTION, penalty);
                tx.setStatus(TransactionStatus.SUCCESS);
                account.addTransaction(tx);
                System.out.println("Deducted penalty " + penalty + " " + account.getCurrency() + " from account " + account.getAccountNumber());
            }
        }
        System.out.println("--- End of Month Processing Complete ---");
    }
}
