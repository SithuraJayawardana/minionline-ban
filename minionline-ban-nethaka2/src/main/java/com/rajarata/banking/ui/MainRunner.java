package com.rajarata.banking.ui;

import com.rajarata.banking.db.DatabaseManager;
import com.rajarata.banking.db.UserDAO;
import com.rajarata.banking.domain.users.Administrator;
import com.rajarata.banking.domain.users.Customer;

import javax.swing.SwingUtilities;
import java.time.LocalDate;
import java.util.Base64;

public class MainRunner {
    public static void main(String[] args) {  
        // 1. Establish SQLite DB and tables
        DatabaseManager.initializeDatabase();
        
        // 2. Safely populate default records if none exist
        UserDAO userDAO = new UserDAO();
        if (userDAO.getAllUsers().isEmpty()) {
            System.out.println("Initializing default dataset...");
            
            Administrator admin = new Administrator("A1", "Super Admin", "admin@bank.com", "011", "SA", "IT");
            admin.setPasswordHash(Base64.getEncoder().encodeToString("admin123".getBytes()));
            userDAO.addUser(admin, "ADMIN");

            Customer cust = new Customer("C1", "Jane Doe", "jane@bank.com", "077", "Colombo", LocalDate.of(1995, 1, 1));
            cust.setPasswordHash(Base64.getEncoder().encodeToString("cust123".getBytes()));
            userDAO.addUser(cust, "CUSTOMER");
            
            // 2.5 Populate Default Accounts
            com.rajarata.banking.db.AccountDAO accountDAO = new com.rajarata.banking.db.AccountDAO();
            com.rajarata.banking.domain.accounts.CheckingAccount checkAcc = new com.rajarata.banking.domain.accounts.CheckingAccount("CHK-1001", cust, 5000.0, 5000.0);
            com.rajarata.banking.domain.accounts.SavingsAccount saveAcc = new com.rajarata.banking.domain.accounts.SavingsAccount("SAV-2001", cust, 15000.0);
            accountDAO.createAccount(checkAcc);
            accountDAO.createAccount(saveAcc);

            System.out.println("✅ System initialized successfully.");
        }

        // 3. Launch UI
        SwingUtilities.invokeLater(() -> {
            ThemeUtil.applyTheme();
            new LoginFrame(userDAO).setVisible(true);
        });
    }
}

