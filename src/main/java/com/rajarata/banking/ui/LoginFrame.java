package com.rajarata.banking.ui;

import com.rajarata.banking.db.UserDAO;
import com.rajarata.banking.domain.users.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private UserDAO userDAO;

    public LoginFrame(UserDAO userDAO) {
        this.userDAO = userDAO;
        setTitle("Rajarata Digital Banking - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        getContentPane().setBackground(ThemeUtil.COLOR_BROWN);
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel titleLabel = new JLabel("Secure Login");
        titleLabel.setFont(ThemeUtil.HEADER_FONT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        add(new JLabel("Email:"), gbc);
        emailField = new JTextField(20);
        gbc.gridx = 1;
        add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(this::handleLogin);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(loginButton, gbc);
        
        JLabel hintLabel = new JLabel("Hint: Try admin@bank.com / admin123");
        hintLabel.setForeground(ThemeUtil.COLOR_YELLOW);
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        add(hintLabel, gbc);

        JButton signUpButton = new JButton("Don't have an account? Sign Up");
        signUpButton.setBackground(ThemeUtil.COLOR_BROWN);
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setFocusPainted(false);
        signUpButton.addActionListener(e -> {
            new SignUpFrame(userDAO).setVisible(true);
            dispose();
        });
        gbc.gridy = 5;
        add(signUpButton, gbc);
    }

    private void handleLogin(ActionEvent e) {
        String email = emailField.getText();
        String pwd = new String(passwordField.getPassword());
        
        // Simple hash check mocking
        String hashed = java.util.Base64.getEncoder().encodeToString(pwd.getBytes());
        User user = userDAO.authenticate(email, hashed);

        if (user != null) {
            JOptionPane.showMessageDialog(this, "Welcome " + user.getName() + "!", "Login Success", JOptionPane.INFORMATION_MESSAGE);
            // Route user window dynamically
            if (user instanceof Administrator) {
                new AdminDashboard((Administrator) user, userDAO).setVisible(true);
            } else {
                new CustomerDashboard((Customer) user).setVisible(true);
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
