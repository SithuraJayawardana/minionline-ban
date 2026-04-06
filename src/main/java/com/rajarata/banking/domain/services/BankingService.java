package com.rajarata.banking.domain.services;

import com.rajarata.banking.db.TransactionDAO;
import com.rajarata.banking.domain.accounts.BankAccount;
import com.rajarata.banking.domain.security.AuditLogger;
import com.rajarata.banking.domain.notifications.NotificationService;
import com.rajarata.banking.domain.transactions.Transaction;
import com.rajarata.banking.domain.transactions.TransactionStatus;
import com.rajarata.banking.domain.transactions.TransactionType;
import com.rajarata.banking.domain.exceptions.AccountNotFoundException;

import java.util.UUID;
// Handles main banking operations like deposit, withdraw, and transfer

public class BankingService {

    private AuditLogger auditLogger;
    private FraudDetectionService fraudDetectionService;
    private NotificationService notificationService;
    private TransactionDAO transactionDAO;

    // simple constructor (no extra services)

    public BankingService() {}

    // constructor with all services (used in real system / UI)
    public BankingService(TransactionDAO transactionDAO,
                          AuditLogger auditLogger,
                          FraudDetectionService fraudDetectionService,
                          NotificationService notificationService) {
        this.transactionDAO       = transactionDAO;
        this.auditLogger          = auditLogger;
        this.fraudDetectionService = fraudDetectionService;
        this.notificationService  = notificationService;
    }

    // returns conversion rate between two currencies
    private double getConversionRate(String fromCur, String toCur) {
        if (fromCur.equalsIgnoreCase(toCur)) return 1.0;
        if (fromCur.equalsIgnoreCase("USD") && toCur.equalsIgnoreCase("LKR")) return 320.0;
        if (fromCur.equalsIgnoreCase("EUR") && toCur.equalsIgnoreCase("LKR")) return 350.0;
        if (fromCur.equalsIgnoreCase("LKR") && toCur.equalsIgnoreCase("USD")) return 1.0 / 320.0;
        if (fromCur.equalsIgnoreCase("LKR") && toCur.equalsIgnoreCase("EUR")) return 1.0 / 350.0;
        return 1.0;// default case
    }

    public void deposit(BankAccount account, double amount) {
         // check if account is valid
        if (account == null) throw new AccountNotFoundException("Account cannot be null.");

        // create transaction for deposit
        Transaction tx = new Transaction(UUID.randomUUID().toString(), TransactionType.DEPOSIT, amount);
        try {
            // perform deposit

            account.deposit(amount);
            tx.setStatus(TransactionStatus.SUCCESS);

            // log successful deposit
            if (auditLogger != null) {
                auditLogger.logEvent("DEPOSIT", "Deposited " + amount + " to " + account.getAccountNumber());
            }
        } catch (Exception e) {
            // mark transaction as failed

            tx.setStatus(TransactionStatus.FAILED);
            // log failure
            if (auditLogger != null) {
                auditLogger.logEvent("DEPOSIT_FAILED", "Failed deposit of " + amount + " to " + account.getAccountNumber() + ": " + e.getMessage());
            }
            throw e;
        } finally {
            // save transaction and monitor for fraud
            account.addTransaction(tx);
            if (transactionDAO != null) transactionDAO.saveTransaction(account.getAccountNumber(), tx);
            if (fraudDetectionService != null) fraudDetectionService.monitorTransaction(account, tx);
        }
    }

    public void withdraw(BankAccount account, double amount) {
        // check if account exists
        if (account == null) throw new AccountNotFoundException("Account cannot be null.");

        // create withdrawal transaction
        Transaction tx = new Transaction(UUID.randomUUID().toString(), TransactionType.WITHDRAWAL, amount);
        try {
            // perform withdrawal
            account.withdraw(amount);
            tx.setStatus(TransactionStatus.SUCCESS);

            // log success
            if (auditLogger != null) {
                auditLogger.logEvent("WITHDRAWAL", "Withdrew " + amount + " from " + account.getAccountNumber());
            }

            // notify if balance is low
            if (account.getBalance() < 1000.0 && notificationService != null) {
                notificationService.notifyAll("LOW_BALANCE", "Account " + account.getAccountNumber() + " balance dropped to " + account.getBalance());
            }
        } catch (Exception e) {
             // mark failure
            tx.setStatus(TransactionStatus.FAILED);

            // log failure
            if (auditLogger != null) {
                auditLogger.logEvent("WITHDRAW_FAILED", "Failed withdrawal of " + amount + " from " + account.getAccountNumber() + ": " + e.getMessage());
            }

            // notify failure
            if (notificationService != null) {
                notificationService.notifyAll("TRANSACTION_FAILED", "Failed withdrawal of " + amount);
            }
            throw e;
        } finally {
            // save and monitor transaction
            account.addTransaction(tx);
            if (transactionDAO != null) transactionDAO.saveTransaction(account.getAccountNumber(), tx);
            if (fraudDetectionService != null) fraudDetectionService.monitorTransaction(account, tx);
        }
    }

    public void transfer(BankAccount fromAccount, BankAccount toAccount, double amount) {
        // check both accounts
        if (fromAccount == null || toAccount == null) throw new AccountNotFoundException("One or both accounts not found.");

        // create transaction IDs

        String txId = UUID.randomUUID().toString();
        Transaction outTx = new Transaction(txId, TransactionType.TRANSFER, amount);

        // convert currency if needed
        double exchangeRate   = getConversionRate(fromAccount.getCurrency(), toAccount.getCurrency());
        double convertedAmount = amount * exchangeRate;
        Transaction inTx = new Transaction(UUID.randomUUID().toString(), TransactionType.TRANSFER, convertedAmount);

        try {
            // withdraw from sender and deposit to receiver
            fromAccount.withdraw(amount);
            toAccount.deposit(convertedAmount);
            outTx.setStatus(TransactionStatus.SUCCESS);
            inTx.setStatus(TransactionStatus.SUCCESS);

            // log success
            if (auditLogger != null) {
                auditLogger.logEvent("TRANSFER", "Transferred " + amount + " from " + fromAccount.getAccountNumber() + " to " + toAccount.getAccountNumber());
            }
            // notify success
            if (notificationService != null) {
                notificationService.notifyAll("TRANSFER_SUCCESS", "Transferred " + amount + " to " + toAccount.getAccountNumber());
            }
        } catch (Exception e) {
            // mark failure
            outTx.setStatus(TransactionStatus.FAILED);
            inTx.setStatus(TransactionStatus.FAILED);

            // log failure
            if (auditLogger != null) {
                auditLogger.logEvent("TRANSFER_FAILED", "Failed transfer of " + amount + " from " + fromAccount.getAccountNumber() + ": " + e.getMessage());
            }

            // notify failure
            if (notificationService != null) {
                notificationService.notifyAll("TRANSACTION_FAILED", "Failed transfer of " + amount);
            }
            throw e;
        } finally {
            // always save sender transaction
            fromAccount.addTransaction(outTx);
            if (transactionDAO != null) transactionDAO.saveTransaction(fromAccount.getAccountNumber(), outTx);
            if (fraudDetectionService != null) fraudDetectionService.monitorTransaction(fromAccount, outTx);

            // save receiver transaction only if success
            if (outTx.getStatus() == TransactionStatus.SUCCESS) {
                toAccount.addTransaction(inTx);
                if (transactionDAO != null) transactionDAO.saveTransaction(toAccount.getAccountNumber(), inTx);
                if (fraudDetectionService != null) fraudDetectionService.monitorTransaction(toAccount, inTx);
            }
        }
    }
}
