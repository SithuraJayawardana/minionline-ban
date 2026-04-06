package com.rajarata.banking.domain.services;

import com.rajarata.banking.domain.accounts.BankAccount;
import com.rajarata.banking.domain.transactions.Transaction;
import com.rajarata.banking.domain.transactions.TransactionStatus;
import com.rajarata.banking.domain.transactions.TransactionType;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;

public class BillPaymentService {

    // list of allowed bill types
    private static final List<String> VALID_BILLS = Arrays.asList("Electricity", "Water", "Internet");
    
    public boolean payUtilityBill(BankAccount account, String billType, double amount) {
        // check if the bill type is valid
        if (!VALID_BILLS.contains(billType)) {
            throw new IllegalArgumentException("Invalid bill type. Accepted: Electricity, Water, Internet.");
        }
        // create a transaction for the bill payment
        Transaction tx = new Transaction(UUID.randomUUID().toString(), TransactionType.BILL_PAYMENT, amount);
        try {
            // withdraw money from the account to pay the bill
            account.withdraw(amount);

            // mark transaction as successful
            tx.setStatus(TransactionStatus.SUCCESS);
            return true;
        } catch (Exception e) {
             // mark transaction as failed if something goes wrong
            tx.setStatus(TransactionStatus.FAILED);
            throw e; 
        } finally {
            // save the transaction in the account history
            account.addTransaction(tx);
        }
    }
}
