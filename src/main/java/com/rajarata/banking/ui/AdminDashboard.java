package com.rajarata.banking.ui;

import com.rajarata.banking.db.TransactionDAO;
import com.rajarata.banking.db.UserDAO;
import com.rajarata.banking.db.AccountDAO;
import com.rajarata.banking.domain.users.*;
import com.rajarata.banking.domain.services.SystemScheduler;
import com.rajarata.banking.domain.accounts.BankAccount;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AdminDashboard extends JFrame {
    private Administrator admin;
    private UserDAO userDAO;
    private TransactionDAO transactionDAO;
    private DefaultTableModel userTableModel;
    private DefaultTableModel txTableModel;
    private JTextArea auditLogArea;
    private SystemScheduler systemScheduler;
    private AccountDAO accountDAO;

    public AdminDashboard(Administrator admin, UserDAO userDAO) {
        this.admin          = admin;
        this.userDAO        = userDAO;
        this.transactionDAO = new TransactionDAO();
        this.accountDAO     = new AccountDAO();
        this.systemScheduler = new SystemScheduler();

        setTitle("Administrator Dashboard — " + admin.getName());
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeUtil.COLOR_BG_PANEL);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ThemeUtil.LABEL_FONT);

        tabbedPane.addTab("👥 User Management",     createUserPanel());
        tabbedPane.addTab("📊 Transaction Overview", createTransactionPanel());
        tabbedPane.addTab("🔒 System Audit Logs",    createAuditLogPanel());
        tabbedPane.addTab("⚙️ System Operations",   createSystemOperationsPanel());
        tabbedPane.addTab("📑 Business Reports",    createBusinessReportsPanel());

        add(tabbedPane);
        refreshUserTable();
        refreshTransactionTable();
    }

    // ─────────────────────────────────────────────────────────────────
    //  USER MANAGEMENT
    // ─────────────────────────────────────────────────────────────────
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeUtil.COLOR_BG_PANEL);

        // Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controls.setBackground(ThemeUtil.COLOR_BROWN);
        controls.setBorder(new EmptyBorder(5, 5, 5, 5));

        JTextField emailField = new JTextField(12);
        JTextField nameField  = new JTextField(12);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"CUSTOMER", "STAFF", "ADMIN"});

        controls.add(makeLabel("Email:"));   controls.add(emailField);
        controls.add(makeLabel("Name:"));    controls.add(nameField);
        controls.add(makeLabel("Role:"));    controls.add(roleBox);

        JButton addButton = new JButton("Add User");
        ThemeUtil.styleButton(addButton);
        addButton.addActionListener(e -> {
            String role = roleBox.getSelectedItem().toString();
            String id   = java.util.UUID.randomUUID().toString();
            User u;
            if (role.equals("ADMIN"))
                u = new Administrator(id, nameField.getText(), emailField.getText(), "0000", "Lvl", "IT");
            else if (role.equals("STAFF"))
                u = new BankStaff(id, nameField.getText(), emailField.getText(), "0000", "EMP", "BR");
            else
                u = new Customer(id, nameField.getText(), emailField.getText(), "0000", "Addr", java.time.LocalDate.now());

            u.setPasswordHash(java.util.Base64.getEncoder().encodeToString("password".getBytes()));
            userDAO.addUser(u, role);
            refreshUserTable();
            emailField.setText(""); nameField.setText("");
            JOptionPane.showMessageDialog(this, "✅ User added. Default password: 'password'", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        controls.add(addButton);
        panel.add(controls, BorderLayout.NORTH);

        // Table
        String[] cols = {"User ID", "Name", "Email", "Role"};
        userTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(userTableModel);
        styleTable(table);
        // Hide verbose ID column visually
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Remove button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottom.setBackground(ThemeUtil.COLOR_BROWN);
        JButton removeButton = new JButton("Remove Selected User");
        ThemeUtil.styleButton(removeButton);
        removeButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String sid = (String) table.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Remove user: " + table.getValueAt(row, 1) + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    userDAO.removeUser(sid);
                    refreshUserTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user row first.", "No selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        bottom.add(removeButton);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────
    //  TRANSACTION OVERVIEW
    // ─────────────────────────────────────────────────────────────────
    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeUtil.COLOR_BG_PANEL);

        String[] cols = {"Transaction ID", "Account", "Type", "Amount (LKR)", "Status", "Timestamp"};
        txTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(txTableModel);
        styleTable(table);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottom.setBackground(ThemeUtil.COLOR_BG_PANEL);
        JButton refreshBtn = new JButton("Refresh");
        ThemeUtil.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> refreshTransactionTable());
        bottom.add(refreshBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────
    //  AUDIT LOG VIEWER
    // ─────────────────────────────────────────────────────────────────
    private JPanel createAuditLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeUtil.COLOR_BG_PANEL);

        JLabel header = new JLabel("Security Audit Log  (security_audit.txt)", SwingConstants.CENTER);
        header.setFont(ThemeUtil.HEADER_FONT);
        header.setForeground(ThemeUtil.COLOR_YELLOW);
        header.setBorder(new EmptyBorder(12, 10, 12, 10));
        panel.add(header, BorderLayout.NORTH);

        auditLogArea = new JTextArea();
        auditLogArea.setEditable(false);
        auditLogArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        auditLogArea.setBackground(new Color(30, 20, 15));
        auditLogArea.setForeground(new Color(180, 255, 150));
        auditLogArea.setCaretColor(Color.WHITE);
        auditLogArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(auditLogArea);
        scrollPane.getViewport().setBackground(new Color(30, 20, 15));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottom.setBackground(ThemeUtil.COLOR_BG_PANEL);
        JButton refreshBtn = new JButton("Refresh Log");
        ThemeUtil.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> refreshAuditLog());
        bottom.add(refreshBtn);

        JButton clearBtn = new JButton("Clear Log File");
        clearBtn.setBackground(new Color(80, 0, 0));
        clearBtn.setForeground(ThemeUtil.COLOR_YELLOW);
        clearBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Clear the entire audit log file permanently?", "Confirm Clear", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Files.writeString(Paths.get("security_audit.txt"), "");
                    refreshAuditLog();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Could not clear log: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        bottom.add(clearBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        // Load initial content
        refreshAuditLog();
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────
    //  SYSTEM OPERATIONS
    // ─────────────────────────────────────────────────────────────────
    private JPanel createSystemOperationsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setBackground(ThemeUtil.COLOR_BG_PANEL);

        JButton endOfMonthBtn = new JButton("Run End of Month Processing");
        ThemeUtil.styleButton(endOfMonthBtn);
        endOfMonthBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Run EOM processing? (Applies interest/penalties to all accounts)", 
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    List<BankAccount> allAccounts = accountDAO.getAllAccounts();
                    systemScheduler.processEndOfMonth(allAccounts);
                    for (BankAccount acc : allAccounts) {
                        accountDAO.updateBalance(acc.getAccountNumber(), acc.getBalance());
                        for (com.rajarata.banking.domain.transactions.Transaction tx : acc.getTransactionHistory()) {
                            transactionDAO.saveTransaction(acc.getAccountNumber(), tx);
                        }
                    }
                    JOptionPane.showMessageDialog(this, "EOM completed on " + allAccounts.size() + " accounts.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshTransactionTable();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error during EOM: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(endOfMonthBtn);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────
    //  BUSINESS REPORTS
    // ─────────────────────────────────────────────────────────────────
    private JPanel createBusinessReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeUtil.COLOR_BG_PANEL);

        JLabel header = new JLabel("Aggregated Business Reports", SwingConstants.CENTER);
        header.setFont(ThemeUtil.HEADER_FONT);
        header.setForeground(ThemeUtil.COLOR_YELLOW);
        header.setBorder(new EmptyBorder(12, 10, 12, 10));
        panel.add(header, BorderLayout.NORTH);

        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        reportArea.setBackground(new Color(250, 250, 250));
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        bottom.setBackground(ThemeUtil.COLOR_BG_PANEL);

        JButton genBtn = new JButton("Generate Comprehensive Report");
        ThemeUtil.styleButton(genBtn);
        genBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append("=== SYSTEM PERFORMANCE REPORT ===\n\n");
            
            // 1. Customer Activity from DB
            sb.append("[1] CUSTOMER & ACCOUNT ACTIVITY (SQLite DB)\n");
            int totalUsers = userDAO.getAllUsers().size();
            sb.append("Total Registered Users: ").append(totalUsers).append("\n");
            
            List<BankAccount> allAccounts = accountDAO.getAllAccounts();
            double totalLiquidity = 0;
            for (BankAccount acc : allAccounts) {
                totalLiquidity += acc.getBalance();
            }
            sb.append("Total Open Accounts: ").append(allAccounts.size()).append("\n");
            sb.append("Total Bank Liquidity: ").append(String.format("%.2f", totalLiquidity)).append("\n\n");
            
            // 2. Transaction Summaries
            sb.append("[2] TRANSACTION SUMMARIES (SQLite DB)\n");
            List<Object[]> allTx = transactionDAO.getAllTransactionRows();
            sb.append("Total Processed Transactions: ").append(allTx.size()).append("\n\n");
            
            // 3. Raw File Handling metrics (Loans)
            sb.append("[3] LOAN PERFORMANCE (Raw txt file: loans_db.txt)\n");
            List<String> rawLoans = com.rajarata.banking.db.FileStorageUtil.readAllLoans();
            sb.append("Total Disbursed Loans: ").append(rawLoans.size()).append("\n");
            double totalLoanAmount = 0;
            for (String loanLine : rawLoans) {
                try {
                    String[] parts = loanLine.split(",");
                    if (parts.length >= 2) {
                        totalLoanAmount += Double.parseDouble(parts[1]);
                    }
                } catch (Exception ignore) {}
            }
            sb.append("Total Loan Principal Issued: ").append(String.format("%.2f", totalLoanAmount)).append("\n\n");
            
            sb.append("=================================");
            reportArea.setText(sb.toString());
        });
        
        bottom.add(genBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────
    //  REFRESH HELPERS
    // ─────────────────────────────────────────────────────────────────
    private void refreshUserTable() {
        userTableModel.setRowCount(0);
        for (User u : userDAO.getAllUsers()) {
            String role = "CUSTOMER";
            if (u instanceof Administrator) role = "ADMIN";
            else if (u instanceof BankStaff)    role = "STAFF";
            userTableModel.addRow(new Object[]{u.getUserId(), u.getName(), u.getEmail(), role});
        }
    }

    private void refreshTransactionTable() {
        txTableModel.setRowCount(0);
        List<Object[]> rows = transactionDAO.getAllTransactionRows();
        for (Object[] row : rows) {
            // Format amount
            row[3] = String.format("%.2f", row[3]);
            txTableModel.addRow(row);
        }
    }

    private void refreshAuditLog() {
        try {
            java.nio.file.Path path = Paths.get("security_audit.txt");
            if (Files.exists(path)) {
                String content = Files.readString(path);
                auditLogArea.setText(content.isEmpty() ? "(No audit entries yet.)" : content);
            } else {
                auditLogArea.setText("(Log file does not exist yet. Perform banking transactions to generate entries.)");
            }
            // Scroll to bottom to show latest entries
            auditLogArea.setCaretPosition(auditLogArea.getDocument().getLength());
        } catch (IOException e) {
            auditLogArea.setText("Error reading audit log: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────
    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ThemeUtil.LABEL_FONT);
        lbl.setForeground(ThemeUtil.COLOR_YELLOW);
        return lbl;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFont(ThemeUtil.LABEL_FONT);
        table.getTableHeader().setFont(ThemeUtil.LABEL_FONT.deriveFont(Font.BOLD));
        table.getTableHeader().setBackground(ThemeUtil.COLOR_MAROON);
        table.getTableHeader().setForeground(ThemeUtil.COLOR_YELLOW);
        table.setBackground(ThemeUtil.COLOR_BG_PANEL);
        table.setForeground(Color.WHITE);
        table.setGridColor(ThemeUtil.COLOR_BROWN);
        table.setSelectionBackground(ThemeUtil.COLOR_MAROON);
        table.setSelectionForeground(Color.WHITE);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
}
