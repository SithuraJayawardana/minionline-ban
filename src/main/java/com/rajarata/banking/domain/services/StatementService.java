package com.rajarata.banking.domain.services;

import com.rajarata.banking.domain.accounts.BankAccount;
import com.rajarata.banking.domain.transactions.Transaction;
import com.rajarata.banking.domain.transactions.TransactionStatus;
import com.rajarata.banking.domain.transactions.TransactionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class StatementService {

    // Generate a statement for the last 30 days
    public String generateMonthlyStatement(BankAccount account) {
        
        // filter successful transactions from the last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        List<Transaction> recentTransactions = account.getTransactionHistory().stream()
                .filter(tx -> tx.getTimestamp() != null && tx.getTimestamp().isAfter(thirtyDaysAgo) && tx.getStatus() == TransactionStatus.SUCCESS)
                .collect(Collectors.toList());

        double totalInterest = 0;// track interest earned
        double totalFees = 0;// track fees and penalties

        // build statement header
        StringBuilder stmt = new StringBuilder();
        stmt.append("=========================================\n");
        stmt.append("       MONTHLY ACCOUNT STATEMENT\n");
        stmt.append("=========================================\n");
        stmt.append("Account Number: ").append(account.getAccountNumber()).append("\n");
        stmt.append("Owner: ").append(account.getOwner().getName()).append("\n");
        stmt.append("Currency: ").append(account.getCurrency()).append("\n");
        stmt.append("Current Balance: ").append(account.getBalance()).append("\n");
        stmt.append("-----------------------------------------\n");
        stmt.append(String.format("%-20s | %-15s | %-10s\n", "Date", "Type", "Amount"));
        stmt.append("-----------------------------------------\n");


        // add each transaction to the statement
        for (Transaction tx : recentTransactions) {
            stmt.append(String.format("%-20s | %-15s | %-10.2f\n", 
                tx.getTimestamp().toString().substring(0, 19), 
                tx.getType(), 
                tx.getAmount()));
            
            // accumulate totals
            if (tx.getType() == TransactionType.INTEREST_CREDIT) {
                totalInterest += tx.getAmount();
            } else if (tx.getType() == TransactionType.FEE_DEDUCTION || tx.getType() == TransactionType.BILL_PAYMENT) {
                totalFees += tx.getAmount();
            }
        }

        // add summary section
        stmt.append("-----------------------------------------\n");
        stmt.append("Summary over last 30 days:\n");
        stmt.append("Total Interest Earned: ").append(String.format("%.2f", totalInterest)).append("\n");
        stmt.append("Total Fees/Penalties: ").append(String.format("%.2f", totalFees)).append("\n");
        stmt.append("=========================================\n");

        return stmt.toString();
    }
}
