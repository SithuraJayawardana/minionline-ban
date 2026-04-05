package com.rajarata.banking.ui;

import com.rajarata.banking.db.AccountDAO;
import com.rajarata.banking.db.UserDAO;
import com.rajarata.banking.domain.accounts.SavingsAccount;
import com.rajarata.banking.domain.users.Customer;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

public class SignUpFrame extends JFrame {
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private UserDAO userDAO;

    public SignUpFrame(UserDAO userDAO) {
        this.userDAO = userDAO;
        setTitle("Rajarata Digital Banking - Sign Up");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeUtil.COLOR_BACKGROUND);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Create an Account");
        titleLabel.setFont(ThemeUtil.HEADER_FONT);
        titleLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        
        // Form Fields
        addLabel("Name:", gbc, 0, 1);
        nameField = new JTextField(20);
        addField(nameField, gbc, 1, 1);

        addLabel("Email:", gbc, 0, 2);
        emailField = new JTextField(20);
        addField(emailField, gbc, 1, 2);

        addLabel("Phone:", gbc, 0, 3);
        phoneField = new JTextField(20);
        addField(phoneField, gbc, 1, 3);

        addLabel("Password:", gbc, 0, 4);
        passwordField = new JPasswordField(20);
        addField(passwordField, gbc, 1, 4);

        JButton registerButton = new JButton("Sign Up");
        ThemeUtil.styleButton(registerButton);
        registerButton.addActionListener(e -> handleRegistration());
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(registerButton, gbc);

        JButton backButton = new JButton("Back to Login");
        ThemeUtil.styleSecondaryButton(backButton);
        backButton.addActionListener(e -> {
            new LoginFrame(userDAO).setVisible(true);
            dispose();
        });
        gbc.gridy = 6;
        add(backButton, gbc);
    }

    private void addLabel(String text, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x; gbc.gridy = y;
        JLabel lbl = new JLabel(text);
        lbl.setFont(ThemeUtil.LABEL_FONT);
        lbl.setForeground(ThemeUtil.COLOR_PRIMARY);
        add(lbl, gbc);
    }

    private void addField(JComponent field, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x; gbc.gridy = y;
        add(field, gbc);
    }

    private void handleRegistration() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pwd = new String(passwordField.getPassword());

        if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Email, and Password are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String userId = UUID.randomUUID().toString();
        Customer newCustomer = new Customer(userId, name, email, phone, "N/A", LocalDate.now());
        String hashed = Base64.getEncoder().encodeToString(pwd.getBytes());
        newCustomer.setPasswordHash(hashed);

        try {
            userDAO.addUser(newCustomer, "CUSTOMER");
            
            // Automatically provision a default Saving Account
            AccountDAO accountDAO = new AccountDAO();
            String accNum = "SAV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            SavingsAccount savings = new SavingsAccount(accNum, newCustomer, 0.0);
            accountDAO.createAccount(savings);

            JOptionPane.showMessageDialog(this, "Registration Successful!\nA default Savings account was created for you.", "Welcome", JOptionPane.INFORMATION_MESSAGE);
            new LoginFrame(userDAO).setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to register: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
