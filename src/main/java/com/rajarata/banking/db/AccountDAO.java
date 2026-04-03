package com.rajarata.banking.db;

import com.rajarata.banking.domain.accounts.*;
import com.rajarata.banking.domain.users.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    
    public void createAccount(BankAccount account) {
        String query = "INSERT INTO accounts (account_number, user_id, balance, account_type, currency) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, account.getAccountNumber());
            pstmt.setString(2, account.getOwner().getUserId());
            pstmt.setDouble(3, account.getBalance());
            String type = "SAVINGS"; 
            if (account instanceof CheckingAccount) type = "CHECKING";
            pstmt.setString(4, type);
            pstmt.setString(5, account.getCurrency());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating account: " + e.getMessage());
        }
    }

    public void updateBalance(String accountNumber, double newBalance) {
        String query = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, accountNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating balance: " + e.getMessage());
        }
    }
    public List<BankAccount> getAccountsForCustomer(Customer owner) {
        List<BankAccount> accounts = new ArrayList<>();
        String query = "SELECT * FROM accounts WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, owner.getUserId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String accNum = rs.getString("account_number");
                    double bal = rs.getDouble("balance");
                    String type = rs.getString("account_type");
                    String curr = rs.getString("currency");
                    
                    BankAccount acc;
                    if ("CHECKING".equalsIgnoreCase(type)) {
                        acc = new CheckingAccount(accNum, owner, bal, 5000.0);
                    } else {
                        acc = new SavingsAccount(accNum, owner, bal);
                    }
                    if (curr != null) acc.setCurrency(curr);
                    accounts.add(acc);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching accounts: " + e.getMessage());
        }
        return accounts;
    }

    public BankAccount getAccountByNumber(String accountNumber) {
        String query = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String accNum = rs.getString("account_number");
                    double bal = rs.getDouble("balance");
                    String type = rs.getString("account_type");
                    String userId = rs.getString("user_id");
                    String curr = rs.getString("currency");
                    
                    Customer dummyOwner = new Customer(userId, "Target User", "", "", "", java.time.LocalDate.now());
                    
                    BankAccount acc;
                    if ("CHECKING".equalsIgnoreCase(type)) {
                        acc = new CheckingAccount(accNum, dummyOwner, bal, 5000.0);
                    } else {
                        acc = new SavingsAccount(accNum, dummyOwner, bal);
                    }
                    if (curr != null) acc.setCurrency(curr);
                    return acc;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching account: " + e.getMessage());
        }
        return null;
    }

    public List<BankAccount> getAllAccounts() {
        List<BankAccount> accounts = new ArrayList<>();
        String query = "SELECT * FROM accounts";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String accNum = rs.getString("account_number");
                double bal = rs.getDouble("balance");
                String type = rs.getString("account_type");
                String userId = rs.getString("user_id");
                String curr = rs.getString("currency");
                
                Customer dummyOwner = new Customer(userId, "Dummy Owner", "", "", "", java.time.LocalDate.now());
                
                BankAccount acc;
                if ("CHECKING".equalsIgnoreCase(type)) {
                    acc = new CheckingAccount(accNum, dummyOwner, bal, 5000.0);
                } else {
                    acc = new SavingsAccount(accNum, dummyOwner, bal);
                }
                if (curr != null) acc.setCurrency(curr);
                accounts.add(acc);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all accounts: " + e.getMessage());
        }
        return accounts;
    }
}
