package com.rajarata.banking.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:rajarata.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            // Create Users Table
            String createUsersSql = "CREATE TABLE IF NOT EXISTS users (" +
                    "user_id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT NOT NULL, " +
                    "phone_number TEXT, " +
                    "role TEXT NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "failed_attempts INTEGER DEFAULT 0, " +
                    "is_locked BOOLEAN DEFAULT 0" +
                    ");";
            stmt.execute(createUsersSql);

            // Create simple Accounts Table for basic persistence
            String createAccountsSql = "CREATE TABLE IF NOT EXISTS accounts (" +
                    "account_number TEXT PRIMARY KEY, " +
                    "user_id TEXT NOT NULL, " +
                    "balance REAL NOT NULL, " +
                    "currency TEXT DEFAULT 'LKR', " +
                    "account_type TEXT NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                    ");";
            stmt.execute(createAccountsSql);
            try {
                stmt.execute("ALTER TABLE accounts ADD COLUMN currency TEXT DEFAULT 'LKR'");
            } catch (SQLException ignore) {}



            // Create Transactions Table for persistent transaction history
            String createTransactionsSql = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "transaction_id TEXT PRIMARY KEY, " +
                    "account_number TEXT NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "FOREIGN KEY (account_number) REFERENCES accounts(account_number)" +
                    ");";
            stmt.execute(createTransactionsSql);

            System.out.println("Database tables initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }
}
