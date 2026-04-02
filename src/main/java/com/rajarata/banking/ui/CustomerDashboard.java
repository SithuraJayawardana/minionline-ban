package com.rajarata.banking.ui;

import com.rajarata.banking.db.AccountDAO;
import com.rajarata.banking.db.TransactionDAO;
import com.rajarata.banking.domain.accounts.BankAccount;
import com.rajarata.banking.domain.notifications.NotificationService;
import com.rajarata.banking.domain.security.AuditLogger;
import com.rajarata.banking.domain.services.BankingService;
import com.rajarata.banking.domain.services.BillPaymentService;
import com.rajarata.banking.domain.services.LoanService;
import com.rajarata.banking.domain.services.StatementService;
import com.rajarata.banking.domain.services.FraudDetectionService;
import com.rajarata.banking.domain.transactions.Transaction;
import com.rajarata.banking.domain.users.Customer;
import com.rajarata.banking.domain.loans.Loan;
import com.rajarata.banking.domain.loans.LoanType;

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
    private BillPaymentService billPaymentService;
    private LoanService loanService;
    private StatementService statementService;
    private FraudDetectionService fraudDetectionService;

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
        this.fraudDetectionService = new FraudDetectionService(null, auditLogger);
        this.bankingService = new BankingService(transactionDAO, auditLogger, fraudDetectionService, notifications);
        this.billPaymentService = new BillPaymentService();
        this.loanService = new LoanService();
        this.statementService = new StatementService();

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
        tabs.addTab("Bill Payments",         createBillPaymentPanel());
        tabs.addTab("Loans",                 createLoansPanel());
        tabs.addTab("Statements",            createStatementsPanel());

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

        JButton openAccountBtn = new JButton("➕ Open New Account");
        ThemeUtil.styleButton(openAccountBtn);
        openAccountBtn.addActionListener(e -> {
            String[] options = {"Savings Account", "Checking Account"};
            int choice = JOptionPane.showOptionDialog(this, "Select the type of account to open:", "Open New Bank Account",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            
            if (choice >= 0) {
                String prefix = choice == 0 ? "SAV-" : "CHK-";
                String accNum = prefix + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                com.rajarata.banking.domain.accounts.BankAccount newAcc;
                if (choice == 0) {
                    newAcc = new com.rajarata.banking.domain.accounts.SavingsAccount(accNum, customer, 0.0);
                } else {
                    newAcc = new com.rajarata.banking.domain.accounts.CheckingAccount(accNum, customer, 0.0, 5000.0);
                }
                accountDAO.createAccount(newAcc);
                JOptionPane.showMessageDialog(this, "Successfully opened new " + options[choice] + ":\n" + accNum, "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAccounts();
            }
        });
        bottom.add(openAccountBtn);

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
    //  BILL PAYMENTS
    // ─────────────────────────────────────────────────────────────────
    private JPanel createBillPaymentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeUtil.COLOR_BG_PANEL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> accSelector = new JComboBox<>();
        accountSelector.addActionListener(e -> {
            accSelector.removeAllItems();
            for (int i = 0; i < accountSelector.getItemCount(); i++) {
                accSelector.addItem(accountSelector.getItemAt(i));
            }
        });

        JComboBox<String> billTypeSelector = new JComboBox<>(new String[]{"Electricity", "Water", "Internet"});
        JTextField amountField = new JTextField(10);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(makeLabel("Select Account:"), gbc);
        gbc.gridx = 1;
        panel.add(accSelector, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(makeLabel("Bill Type:"), gbc);
        gbc.gridx = 1;
        panel.add(billTypeSelector, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(makeLabel("Amount (LKR):"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);

        JButton payBtn = new JButton("Pay Bill");
        ThemeUtil.styleButton(payBtn);
        payBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String accNum = (String) accSelector.getSelectedItem();
                if (accNum == null) throw new IllegalArgumentException("No account selected.");
                BankAccount account = accountDAO.getAccountByNumber(accNum);
                if (account == null) throw new IllegalArgumentException("Account not found.");
                
                billPaymentService.payUtilityBill(account, (String)billTypeSelector.getSelectedItem(), amount);
                accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());
                transactionDAO.saveTransaction(accNum, account.getTransactionHistory().get(account.getTransactionHistory().size() - 1));
                JOptionPane.showMessageDialog(this, "✅ Bill paid successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAccounts();
                amountField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(payBtn, gbc);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────
    //  LOANS
    // ─────────────────────────────────────────────────────────────────
    private JPanel createLoansPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeUtil.COLOR_BG_PANEL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<LoanType> loanTypeBox = new JComboBox<>(LoanType.values());
        JTextField amountField = new JTextField(10);
        JTextField termField = new JTextField(10);
        JComboBox<String> accSelector = new JComboBox<>();
        accountSelector.addActionListener(e -> {
            accSelector.removeAllItems();
            for (int i = 0; i < accountSelector.getItemCount(); i++) {
                accSelector.addItem(accountSelector.getItemAt(i));
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; panel.add(makeLabel("Disbursement Account:"), gbc);
        gbc.gridx = 1; panel.add(accSelector, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(makeLabel("Loan Type:"), gbc);
        gbc.gridx = 1; panel.add(loanTypeBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(makeLabel("Loan Amount (LKR):"), gbc);
        gbc.gridx = 1; panel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(makeLabel("Term (Months):"), gbc);
        gbc.gridx = 1; panel.add(termField, gbc);

        JButton applyBtn = new JButton("Apply for Loan");
        ThemeUtil.styleButton(applyBtn);
        applyBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                int term = Integer.parseInt(termField.getText());
                LoanType type = (LoanType) loanTypeBox.getSelectedItem();
                String accNum = (String) accSelector.getSelectedItem();
                if (accNum == null) throw new IllegalArgumentException("No account selected.");

                BankAccount account = accountDAO.getAccountByNumber(accNum);
                Loan loan = loanService.applyForLoan(customer, amount, term, type);
                double monthly = loanService.calculateMonthlyRepayment(loan);
                
                int confirm = JOptionPane.showConfirmDialog(this, 
                    String.format("Approved! Monthly Sub: %.2f LKR.\nAccept?", monthly), "Loan Offer", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    bankingService.deposit(account, amount);
                    accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());
                    JOptionPane.showMessageDialog(this, "✅ Loan funds disbursed!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshAccounts();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Details error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; panel.add(applyBtn, gbc);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────
    //  STATEMENTS
    // ─────────────────────────────────────────────────────────────────
    private JPanel createStatementsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeUtil.COLOR_BG_PANEL);

        JPanel top = new JPanel(new FlowLayout());
        top.setBackground(ThemeUtil.COLOR_BROWN);
        JComboBox<String> accSelector = new JComboBox<>();
        accountSelector.addActionListener(e -> {
            accSelector.removeAllItems();
            for (int i = 0; i < accountSelector.getItemCount(); i++) {
                accSelector.addItem(accountSelector.getItemAt(i));
            }
        });
        
        top.add(makeLabel("Select Account:"));
        top.add(accSelector);
        JButton genBtn = new JButton("Generate");
        ThemeUtil.styleButton(genBtn);
        top.add(genBtn);
        panel.add(top, BorderLayout.NORTH);

        JTextArea stmtArea = new JTextArea();
        stmtArea.setEditable(false);
        stmtArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        stmtArea.setBackground(new Color(250, 250, 250));
        panel.add(new JScrollPane(stmtArea), BorderLayout.CENTER);

        genBtn.addActionListener(e -> {
            try {
                String accNum = (String) accSelector.getSelectedItem();
                if (accNum == null) throw new IllegalArgumentException("No account selected.");
                BankAccount account = accountDAO.getAccountByNumber(accNum);
                // We must hydrate the full history for the statement
                List<Transaction> txHistory = transactionDAO.getTransactionsForAccount(accNum);
                for (Transaction tx : txHistory) {
                    account.addTransaction(tx);
                }
                String stmt = statementService.generateMonthlyStatement(account);
                stmtArea.setText(stmt);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

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
