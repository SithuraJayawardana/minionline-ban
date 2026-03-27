package com.rajarata.banking.domain.services;

import com.rajarata.banking.domain.accounts.BankAccount;
import com.rajarata.banking.domain.transactions.Transaction;
import com.rajarata.banking.domain.transactions.TransactionStatus;
import com.rajarata.banking.domain.transactions.TransactionType;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;

public class BillPaymentService {
    private static final List<String> VALID_BILLS = Arrays.asList("Electricity", "Water", "Internet");
    
    public boolean payUtilityBill(BankAccount account, String billType, double amount) {
        if (!VALID_BILLS.contains(billType)) {
            throw new IllegalArgumentException("Invalid bill type. Accepted: Electricity, Water, Internet.");
        }
        
        Transaction tx = new Transaction(UUID.randomUUID().toString(), TransactionType.BILL_PAYMENT, amount);
        try {
            // Polymorphic withdrawal handling constraints
            account.withdraw(amount);
            tx.setStatus(TransactionStatus.SUCCESS);
            return true;
        } catch (Exception e) {
            tx.setStatus(TransactionStatus.FAILED);
            throw e; 
        } finally {
            account.addTransaction(tx);
        }
    }
}
