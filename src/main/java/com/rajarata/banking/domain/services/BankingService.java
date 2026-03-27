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

/**
 * Service class handling core business logic for banking operations.
 * All transactions are now persisted to the database via TransactionDAO.
 */
public class BankingService {

    private AuditLogger auditLogger;
    private FraudDetectionService fraudDetectionService;
    private NotificationService notificationService;
    private TransactionDAO transactionDAO;

    /** Default constructor (no persistence / audit). */
    public BankingService() {}

    /** Fully-wired constructor used by the UI. */
    public BankingService(TransactionDAO transactionDAO,
                          AuditLogger auditLogger,
                          FraudDetectionService fraudDetectionService,
                          NotificationService notificationService) {
        this.transactionDAO       = transactionDAO;
        this.auditLogger          = auditLogger;
        this.fraudDetectionService = fraudDetectionService;
        this.notificationService  = notificationService;
    }

    private double getConversionRate(String fromCur, String toCur) {
        if (fromCur.equalsIgnoreCase(toCur)) return 1.0;
        if (fromCur.equalsIgnoreCase("USD") && toCur.equalsIgnoreCase("LKR")) return 320.0;
        if (fromCur.equalsIgnoreCase("EUR") && toCur.equalsIgnoreCase("LKR")) return 350.0;
        if (fromCur.equalsIgnoreCase("LKR") && toCur.equalsIgnoreCase("USD")) return 1.0 / 320.0;
        if (fromCur.equalsIgnoreCase("LKR") && toCur.equalsIgnoreCase("EUR")) return 1.0 / 350.0;
        return 1.0;
    }

    public void deposit(BankAccount account, double amount) {
        if (account == null) throw new AccountNotFoundException("Account cannot be null.");

        Transaction tx = new Transaction(UUID.randomUUID().toString(), TransactionType.DEPOSIT, amount);
        try {
            account.deposit(amount);
            tx.setStatus(TransactionStatus.SUCCESS);
            if (auditLogger != null) {
                auditLogger.logEvent("DEPOSIT", "Deposited " + amount + " to " + account.getAccountNumber());
            }
        } catch (Exception e) {
            tx.setStatus(TransactionStatus.FAILED);
            if (auditLogger != null) {
                auditLogger.logEvent("DEPOSIT_FAILED", "Failed deposit of " + amount + " to " + account.getAccountNumber() + ": " + e.getMessage());
            }
            throw e;
        } finally {
            account.addTransaction(tx);
            if (transactionDAO != null) transactionDAO.saveTransaction(account.getAccountNumber(), tx);
            if (fraudDetectionService != null) fraudDetectionService.monitorTransaction(account, tx);
        }
    }

    public void withdraw(BankAccount account, double amount) {
        if (account == null) throw new AccountNotFoundException("Account cannot be null.");

        Transaction tx = new Transaction(UUID.randomUUID().toString(), TransactionType.WITHDRAWAL, amount);
        try {
            account.withdraw(amount);
            tx.setStatus(TransactionStatus.SUCCESS);
            if (auditLogger != null) {
                auditLogger.logEvent("WITHDRAWAL", "Withdrew " + amount + " from " + account.getAccountNumber());
            }
            if (account.getBalance() < 1000.0 && notificationService != null) {
                notificationService.notifyAll("LOW_BALANCE", "Account " + account.getAccountNumber() + " balance dropped to " + account.getBalance());
            }
        } catch (Exception e) {
            tx.setStatus(TransactionStatus.FAILED);
            if (auditLogger != null) {
                auditLogger.logEvent("WITHDRAW_FAILED", "Failed withdrawal of " + amount + " from " + account.getAccountNumber() + ": " + e.getMessage());
            }
            if (notificationService != null) {
                notificationService.notifyAll("TRANSACTION_FAILED", "Failed withdrawal of " + amount);
            }
            throw e;
        } finally {
            account.addTransaction(tx);
            if (transactionDAO != null) transactionDAO.saveTransaction(account.getAccountNumber(), tx);
            if (fraudDetectionService != null) fraudDetectionService.monitorTransaction(account, tx);
        }
    }

    public void transfer(BankAccount fromAccount, BankAccount toAccount, double amount) {
        if (fromAccount == null || toAccount == null) throw new AccountNotFoundException("One or both accounts not found.");

        String txId = UUID.randomUUID().toString();
        Transaction outTx = new Transaction(txId, TransactionType.TRANSFER, amount);

        double exchangeRate   = getConversionRate(fromAccount.getCurrency(), toAccount.getCurrency());
        double convertedAmount = amount * exchangeRate;
        Transaction inTx = new Transaction(UUID.randomUUID().toString(), TransactionType.TRANSFER, convertedAmount);

        try {
            fromAccount.withdraw(amount);
            toAccount.deposit(convertedAmount);
            outTx.setStatus(TransactionStatus.SUCCESS);
            inTx.setStatus(TransactionStatus.SUCCESS);
            if (auditLogger != null) {
                auditLogger.logEvent("TRANSFER", "Transferred " + amount + " from " + fromAccount.getAccountNumber() + " to " + toAccount.getAccountNumber());
            }
            if (notificationService != null) {
                notificationService.notifyAll("TRANSFER_SUCCESS", "Transferred " + amount + " to " + toAccount.getAccountNumber());
            }
        } catch (Exception e) {
            outTx.setStatus(TransactionStatus.FAILED);
            inTx.setStatus(TransactionStatus.FAILED);
            if (auditLogger != null) {
                auditLogger.logEvent("TRANSFER_FAILED", "Failed transfer of " + amount + " from " + fromAccount.getAccountNumber() + ": " + e.getMessage());
            }
            if (notificationService != null) {
                notificationService.notifyAll("TRANSACTION_FAILED", "Failed transfer of " + amount);
            }
            throw e;
        } finally {
            fromAccount.addTransaction(outTx);
            if (transactionDAO != null) transactionDAO.saveTransaction(fromAccount.getAccountNumber(), outTx);
            if (fraudDetectionService != null) fraudDetectionService.monitorTransaction(fromAccount, outTx);

            if (outTx.getStatus() == TransactionStatus.SUCCESS) {
                toAccount.addTransaction(inTx);
                if (transactionDAO != null) transactionDAO.saveTransaction(toAccount.getAccountNumber(), inTx);
                if (fraudDetectionService != null) fraudDetectionService.monitorTransaction(toAccount, inTx);
            }
        }
    }
}
