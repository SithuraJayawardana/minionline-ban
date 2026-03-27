package com.rajarata.banking.ui;

import com.rajarata.banking.db.AccountDAO;
import com.rajarata.banking.db.TransactionDAO;
import com.rajarata.banking.domain.accounts.BankAccount;
import com.rajarata.banking.domain.notifications.NotificationService;
import com.rajarata.banking.domain.security.AuditLogger;
import com.rajarata.banking.domain.services.BankingService;
import com.rajarata.banking.domain.transactions.Transaction;
import com.rajarata.banking.domain.users.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerDashboard extends JFrame {
    private Customer customer;
    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;
    private BankingService bankingService;
    private DefaultTableModel accountTableModel;
    private DefaultTableModel txTableModel;
    private JComboBox<String> accountSelector;

    public CustomerDashboard(Customer customer) {
        this.customer = customer;

        // --- Wire all backend services ---
        this.accountDAO      = new AccountDAO();
        this.transactionDAO  = new TransactionDAO();
        AuditLogger auditLogger          = new AuditLogger();
        NotificationService notifications = new NotificationService();
        // Add a Swing-based notification observer that pops alerts to the user
        notifications.addObserver((eventType, message) ->
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, eventType, JOptionPane.INFORMATION_MESSAGE)
            )
        );
        // FraudDetectionService requires an admin reference — omit for customer sessions (null-safe)
        this.bankingService = new BankingService(transactionDAO, auditLogger, null, notifications);

        setTitle("Rajarata Digital Banking — " + customer.getName());
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeUtil.COLOR_BG_PANEL);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(ThemeUtil.LABEL_FONT);

        tabs.addTab("Account Overview",      createOverviewPanel());
        tabs.addTab("Transactions",          createTransactionsPanel());
        tabs.addTab("Transaction History",   createHistoryPanel());

        add(tabs);
        refreshAccounts();
    }

    // ─────────────────────────────────────────────────────────────────
    //  ACCOUNT OVERVIEW
    // ─────────────────────────────────────────────────────────────────
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeUtil.COLOR_BROWN);

        JLabel welcome = new JLabel("Welcome back, " + customer.getName(), SwingConstants.CENTER);
        welcome.setFont(ThemeUtil.HEADER_FONT);
        welcome.setForeground(ThemeUtil.COLOR_YELLOW);
        welcome.setBorder(new EmptyBorder(15, 10, 15, 10));
        panel.add(welcome, BorderLayout.NORTH);

        String[] cols = {"Account Number", "Type", "Balance (LKR)"};
        accountTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(accountTableModel);
        styleTable(table);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottom.setBackground(ThemeUtil.COLOR_BROWN);
        JButton refreshBtn = new JButton("Refresh Balances");
        ThemeUtil.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> refreshAccounts());
        bottom.add(refreshBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────
    //  MAKE A TRANSACTION
    // ─────────────────────────────────────────────────────────────────
    private JPanel createTransactionsPanel() {
        JPanel txPanel = new JPanel(new GridBagLayout());
        txPanel.setBackground(ThemeUtil.COLOR_BG_PANEL);
        txPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        accountSelector = new JComboBox<>();
        JTextField amountField = new JTextField(15);
        JTextField targetAccountField = new JTextField(15);

        // Row 0 — account selector
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        txPanel.add(makeLabel("Select Account:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txPanel.add(accountSelector, gbc);

        // Row 1 — amount
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        txPanel.add(makeLabel("Amount (LKR):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txPanel.add(amountField, gbc);

        // Row 2 — target account (for transfers)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        txPanel.add(makeLabel("Target Account (Transfer):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txPanel.add(targetAccountField, gbc);

        // Row 3 — action buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setBackground(ThemeUtil.COLOR_BG_PANEL);

        JButton depositBtn  = new JButton("💰 Deposit");
        JButton withdrawBtn = new JButton("💸 Withdraw");
        JButton transferBtn = new JButton("🔁 Transfer");
        for (JButton btn : new JButton[]{depositBtn, withdrawBtn, transferBtn}) ThemeUtil.styleButton(btn);

        depositBtn.addActionListener(e ->
            handleTransaction("DEPOSIT", amountField.getText(), "", amountField));
        withdrawBtn.addActionListener(e ->
            handleTransaction("WITHDRAW", amountField.getText(), "", amountField));
        transferBtn.addActionListener(e ->
            handleTransaction("TRANSFER", amountField.getText(), targetAccountField.getText(), amountField));

        btnPanel.add(depositBtn);
        btnPanel.add(withdrawBtn);
        btnPanel.add(transferBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        txPanel.add(btnPanel, gbc);

        return txPanel;
    }

    // ─────────────────────────────────────────────────────────────────
    //  TRANSACTION HISTORY
    // ─────────────────────────────────────────────────────────────────
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeUtil.COLOR_BG_PANEL);

        String[] cols = {"Transaction ID", "Account", "Type", "Amount (LKR)", "Status", "Timestamp"};
        txTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(txTableModel);
        styleTable(table);
        // Hide the verbose Transaction ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottom.setBackground(ThemeUtil.COLOR_BG_PANEL);
        JButton refreshBtn = new JButton("Refresh History");
        ThemeUtil.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> refreshHistory());
        bottom.add(refreshBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────
    //  TRANSACTION HANDLER
    // ─────────────────────────────────────────────────────────────────
    private void handleTransaction(String type, String amountStr, String targetAccNum, JTextField amountField) {
        try {
            double amount = Double.parseDouble(amountStr.trim());
            String selectedAccNum = (String) accountSelector.getSelectedItem();
            if (selectedAccNum == null) throw new IllegalArgumentException("No account selected.");

            BankAccount account = accountDAO.getAccountByNumber(selectedAccNum);
            if (account == null) throw new IllegalArgumentException("Account not found in database.");

            switch (type) {
                case "DEPOSIT":
                    bankingService.deposit(account, amount);
                    accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());
                    JOptionPane.showMessageDialog(this, "✅ Deposit of LKR " + amount + " successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "WITHDRAW":
                    bankingService.withdraw(account, amount);
                    accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());
                    JOptionPane.showMessageDialog(this, "✅ Withdrawal of LKR " + amount + " successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "TRANSFER":
                    BankAccount targetAccount = accountDAO.getAccountByNumber(targetAccNum.trim());
                    if (targetAccount == null) throw new IllegalArgumentException("Target account '" + targetAccNum + "' not found.");
                    bankingService.transfer(account, targetAccount, amount);
                    accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());
                    accountDAO.updateBalance(targetAccount.getAccountNumber(), targetAccount.getBalance());
                    JOptionPane.showMessageDialog(this, "✅ Transfer of LKR " + amount + " to " + targetAccNum + " successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    break;
            }
            amountField.setText("");
            refreshAccounts();
            refreshHistory();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Transaction Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  REFRESH HELPERS
    // ─────────────────────────────────────────────────────────────────
    private void refreshAccounts() {
        accountTableModel.setRowCount(0);
        if (accountSelector != null) accountSelector.removeAllItems();

        List<BankAccount> accounts = accountDAO.getAccountsForCustomer(customer);
        for (BankAccount acc : accounts) {
            String type = acc instanceof com.rajarata.banking.domain.accounts.CheckingAccount ? "Checking" : "Savings";
            accountTableModel.addRow(new Object[]{acc.getAccountNumber(), type, String.format("%.2f", acc.getBalance())});
            if (accountSelector != null) accountSelector.addItem(acc.getAccountNumber());
        }
        refreshHistory();
    }

    private void refreshHistory() {
        if (txTableModel == null) return;
        txTableModel.setRowCount(0);
        List<BankAccount> accounts = accountDAO.getAccountsForCustomer(customer);
        for (BankAccount acc : accounts) {
            List<Transaction> txList = transactionDAO.getTransactionsForAccount(acc.getAccountNumber());
            String accType = acc instanceof com.rajarata.banking.domain.accounts.CheckingAccount ? "Checking" : "Savings";
            for (Transaction tx : txList) {
                txTableModel.addRow(new Object[]{
                    tx.getTransactionId(),
                    acc.getAccountNumber() + " (" + accType + ")",
                    tx.getType().name(),
                    String.format("%.2f", tx.getAmount()),
                    tx.getStatus().name(),
                    tx.getTimestamp().toString().replace("T", " ")
                });
            }
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
