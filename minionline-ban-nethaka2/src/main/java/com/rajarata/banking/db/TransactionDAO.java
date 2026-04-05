package com.rajarata.banking.db;

import com.rajarata.banking.domain.transactions.Transaction;
import com.rajarata.banking.domain.transactions.TransactionStatus;
import com.rajarata.banking.domain.transactions.TransactionType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//DAO for persisting and querying Transaction records.
 
public class TransactionDAO {

    // Saves a completed transaction linked to a specific account number.
     
    public void saveTransaction(String accountNumber, Transaction tx) {
        String sql = "INSERT OR IGNORE INTO transactions " +
                     "(transaction_id, account_number, type, amount, status, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tx.getTransactionId());
            pstmt.setString(2, accountNumber);
            pstmt.setString(3, tx.getType().name());
            pstmt.setDouble(4, tx.getAmount());
            pstmt.setString(5, tx.getStatus().name());
            pstmt.setString(6, tx.getTimestamp().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving transaction: " + e.getMessage());
        }
    }

    /**
     * Returns all transactions for a given account number, newest first.
     */
    public List<Transaction> getTransactionsForAccount(String accountNumber) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY timestamp DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id     = rs.getString("transaction_id");
                    String type   = rs.getString("type");
                    double amount = rs.getDouble("amount");
                    String status = rs.getString("status");
                    String tsStr  = rs.getString("timestamp");

                    Transaction tx = new Transaction(id, TransactionType.valueOf(type), amount);
                    tx.setStatus(TransactionStatus.valueOf(status));
                    if (tsStr != null) tx.setTimestamp(LocalDateTime.parse(tsStr));
                    list.add(tx);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all transactions across all accounts (for admin overview).
     */
    public List<Object[]> getAllTransactionRows() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT transaction_id, account_number, type, amount, status, timestamp " +
                     "FROM transactions ORDER BY timestamp DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString("transaction_id"),
                    rs.getString("account_number"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("status"),
                    rs.getString("timestamp")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all transactions: " + e.getMessage());
        }
        return rows;
    }
}
