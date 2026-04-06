package com.rajarata.banking.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.rajarata.banking.db.AccountDAO;
import com.rajarata.banking.db.TransactionDAO;
import com.rajarata.banking.domain.accounts.BankAccount;
import com.rajarata.banking.domain.loans.Loan;
import com.rajarata.banking.domain.loans.LoanType;
import com.rajarata.banking.domain.notifications.NotificationService;
import com.rajarata.banking.domain.security.AuditLogger;
import com.rajarata.banking.domain.services.BankingService;
import com.rajarata.banking.domain.services.BillPaymentService;
import com.rajarata.banking.domain.services.FraudDetectionService;
import com.rajarata.banking.domain.services.LoanService;
import com.rajarata.banking.domain.services.StatementService;
import com.rajarata.banking.domain.transactions.Transaction;
import com.rajarata.banking.domain.users.Customer;

/**
 * Modern customer dashboard with professional design.
 * Features: Account management, transactions, bills, loans, statements.
 */
public class CustomerDashboard extends JFrame {
    private Customer customer;
    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;
    private BankingService bankingService;
    private BillPaymentService billPaymentService;
    private LoanService loanService;
    private StatementService statementService;
    private FraudDetectionService fraudDetectionService;
    
    private DefaultTableModel accountTableModel;
    private DefaultTableModel txTableModel;
    private JComboBox<String> accountSelector;
    private JPanel statsPanel;  // Store reference to stats panel for refreshing

    public CustomerDashboard(Customer customer) {
        this.customer = customer;

        // Wire all backend services
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
        AuditLogger auditLogger = new AuditLogger();
        NotificationService notifications = new NotificationService();
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

        // Frame setup
        setTitle("Rajarata Digital Bank — " + customer.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Create main layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ThemeUtil.COLOR_BACKGROUND);

        // Header
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Tabbed Content
        JTabbedPane tabbedPane = createTabbedContent();
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
        refreshAccounts();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout(16, 0));
        headerPanel.setBackground(ThemeUtil.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(12, 24, 12, 24));

        // Left: Logo and Bank branding
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setBackground(ThemeUtil.COLOR_PRIMARY);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        ImageIcon logoIcon = ThemeUtil.loadLogoIcon(30, 30);
        if (logoIcon != null) {
            leftPanel.add(new JLabel(logoIcon));
        } else {
            JLabel logoPlaceholder = new JLabel("🏦");
            Font emojiFont = createEmojiSupportingFont(24);
            logoPlaceholder.setFont(emojiFont);
            leftPanel.add(logoPlaceholder);
        }
        
        JLabel brandLabel = new JLabel("RAJARATA DIGITAL BANK");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brandLabel.setForeground(ThemeUtil.COLOR_WHITE);
        leftPanel.add(brandLabel);

        // Center: Welcome message with proper alignment
        JLabel welcomeLabel = new JLabel("Welcome, " + customer.getName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setForeground(ThemeUtil.COLOR_WHITE);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(ThemeUtil.COLOR_PRIMARY);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        centerPanel.add(welcomeLabel, BorderLayout.CENTER);

        // Right: Logout button (modern, clean, no borders)
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(ThemeUtil.COLOR_WHITE);
        logoutBtn.setForeground(ThemeUtil.COLOR_PRIMARY);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setContentAreaFilled(true);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                logoutBtn.setBackground(new Color(230, 230, 230));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                logoutBtn.setBackground(ThemeUtil.COLOR_WHITE);
            }
        });
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame(new com.rajarata.banking.db.UserDAO()).setVisible(true);
        });

        // Right panel with logout button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(ThemeUtil.COLOR_PRIMARY);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rightPanel.add(logoutBtn);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(centerPanel, BorderLayout.CENTER);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JTabbedPane createTabbedContent() {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setBackground(ThemeUtil.COLOR_BACKGROUND);
        tabbedPane.setForeground(ThemeUtil.COLOR_TEXT_DARK);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Remove borders and focus painting for clean look
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Add tabs with clean labels (no emoji to avoid rendering issues)
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Accounts", createAccountsPanel());
        tabbedPane.addTab("Transactions", createTransactionsPanel());
        tabbedPane.addTab("History", createHistoryPanel());
        tabbedPane.addTab("Bills", createBillPaymentPanel());
        tabbedPane.addTab("Loans", createLoansPanel());
        tabbedPane.addTab("Statements", createStatementsPanel());

        // ===== CUSTOM TAB APPEARANCE =====
        // Style the tab appearance to be clean and modern
        UIManager.put("TabbedPane.selected", ThemeUtil.COLOR_ACCENT);
        UIManager.put("TabbedPane.selectHighlight", ThemeUtil.COLOR_ACCENT);
        UIManager.put("TabbedPane.unselectedBackground", ThemeUtil.COLOR_BACKGROUND);
        
        // Create custom tab UI with underline effect for selected tab
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, 
                                         int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected) {
                    // Gold underline for selected tab (3px height)
                    g2.setColor(ThemeUtil.COLOR_ACCENT);
                    g2.setStroke(new java.awt.BasicStroke(3));
                    g2.drawLine(x, y + h - 2, x + w, y + h - 2);
                }
                // Don't paint default border for unselected tabs
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // Remove default content border for clean look
                g.setColor(ThemeUtil.COLOR_BACKGROUND);
                g.fillRect(0, 0, tabPane.getWidth(), tabPane.getHeight());
            }
        });

        return tabbedPane;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeUtil.COLOR_BACKGROUND);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // ===== SECTION 1: Quick Statistics Cards =====
        JPanel statsPanel = createStatsSection();
        panel.add(statsPanel, BorderLayout.NORTH);

        // ===== SECTION 2: Quick Action Cards Grid =====
        JPanel actionPanel = createActionCardsSection();
        panel.add(actionPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the statistics section with 3 stat cards
     * Dynamically calculates total accounts, balance, and transaction count
     */
    private JPanel createStatsSection() {
        statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(ThemeUtil.COLOR_BACKGROUND);
        statsPanel.setBorder(new EmptyBorder(0, 0, 32, 0));

        // Calculate statistics from database
        List<BankAccount> accounts = accountDAO.getAccountsForCustomer(customer);
        int totalAccounts = accounts != null ? accounts.size() : 0;
        
        double totalBalance = 0.0;
        int totalTransactions = 0;
        if (accounts != null) {
            for (BankAccount acc : accounts) {
                totalBalance += acc.getBalance();
                List<Transaction> txList = transactionDAO.getTransactionsForAccount(acc.getAccountNumber());
                if (txList != null) {
                    totalTransactions += txList.size();
                }
            }
        }

        // Create stat cards with calculated values
        statsPanel.add(createStatCard("Total Accounts", String.valueOf(totalAccounts), ThemeUtil.COLOR_PRIMARY));
        statsPanel.add(createStatCard("Total Balance", String.format("LKR %.2f", totalBalance), ThemeUtil.COLOR_ACCENT));
        statsPanel.add(createStatCard("Transactions", String.valueOf(totalTransactions), ThemeUtil.COLOR_SUCCESS));

        return statsPanel;
    }

    /**
     * Creates the action cards section with 3x2 grid layout
     * Ensures equal spacing and alignment
     */
    private JPanel createActionCardsSection() {
        JPanel section = new JPanel(new GridLayout(2, 3, 20, 20));
        section.setBackground(ThemeUtil.COLOR_BACKGROUND);
        section.setBorder(new EmptyBorder(16, 0, 0, 0));

        // Add all action cards
        section.add(createModernActionCard("💰", "Deposit", "Add funds", e -> openDepositDialog()));
        section.add(createModernActionCard("💸", "Withdraw", "Withdraw funds", e -> openWithdrawDialog()));
        section.add(createModernActionCard("🔁", "Transfer", "Move between accounts", e -> openTransferDialog()));
        section.add(createModernActionCard("💳", "Pay Bill", "Bill payment", e -> openBillPaymentDialog()));
        section.add(createModernActionCard("📈", "Loan", "Apply for loan", e -> openLoanDialog()));
        section.add(createModernActionCard("🔄", "Refresh", "Refresh data", e -> refreshAccounts()));

        return section;
    }

    private JPanel createAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeUtil.COLOR_BACKGROUND);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("📊 My Accounts");
        // Fix emoji display
        Font emojiFont = createEmojiSupportingFont(ThemeUtil.HEADER_FONT.getSize());
        titleLabel.setFont(new Font(emojiFont.getName(), ThemeUtil.HEADER_FONT.getStyle(), emojiFont.getSize()));
        titleLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] cols = {"Account Number", "Type", "Balance", "Currency"};
        accountTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(accountTableModel);
        styleModernTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(ThemeUtil.COLOR_BACKGROUND);

        JButton newAccountBtn = new JButton("➕ Open Account");
        styleButton(newAccountBtn);
        newAccountBtn.addActionListener(e -> showNewAccountDialog());
        buttonPanel.add(newAccountBtn);

        JButton interestBtn = new JButton("📈 Interest Simulator");
        styleButton(interestBtn);
        interestBtn.addActionListener(e -> showInterestSimulator());
        buttonPanel.add(interestBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeUtil.COLOR_BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("💸 Make a Transaction");
        // Fix emoji display
        Font emojiFont = createEmojiSupportingFont(ThemeUtil.HEADER_FONT.getSize());
        titleLabel.setFont(new Font(emojiFont.getName(), ThemeUtil.HEADER_FONT.getStyle(), emojiFont.getSize()));
        titleLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(createStyledLabel("Select Account:"), gbc);
        accountSelector = new JComboBox<>();
        gbc.gridx = 1;
        panel.add(accountSelector, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(createStyledLabel("Amount (LKR):"), gbc);
        JTextField amountField = new JTextField(20);
        styleTextField(amountField);
        gbc.gridx = 1;
        panel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(createStyledLabel("Target Account:"), gbc);
        JTextField targetField = new JTextField(20);
        styleTextField(targetField);
        gbc.gridx = 1;
        panel.add(targetField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(ThemeUtil.COLOR_BACKGROUND);

        JButton depositBtn = new JButton("💰 Deposit");
        styleButton(depositBtn);
        depositBtn.addActionListener(e -> performTransaction("DEPOSIT", amountField, targetField, amountField));

        JButton withdrawBtn = new JButton("💸 Withdraw");
        styleButton(withdrawBtn);
        withdrawBtn.addActionListener(e -> performTransaction("WITHDRAW", amountField, targetField, amountField));

        JButton transferBtn = new JButton("🔁 Transfer");
        styleButton(transferBtn);
        transferBtn.addActionListener(e -> performTransaction("TRANSFER", amountField, targetField, amountField));

        btnPanel.add(depositBtn);
        btnPanel.add(withdrawBtn);
        btnPanel.add(transferBtn);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeUtil.COLOR_BACKGROUND);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("📋 Transaction History");
        titleLabel.setFont(ThemeUtil.HEADER_FONT);
        titleLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] cols = {"Date & Time", "Account", "Type", "Amount", "Status"};
        txTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(txTableModel);
        styleModernTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ThemeUtil.COLOR_BACKGROUND);
        JButton refreshBtn = new JButton("🔄 Refresh");
        styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> {
            refreshAccounts();  // Refresh both accounts AND history
        });
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBillPaymentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeUtil.COLOR_BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("💳 Pay Bills");
        // Fix emoji display
        Font emojiFont = createEmojiSupportingFont(ThemeUtil.HEADER_FONT.getSize());
        titleLabel.setFont(new Font(emojiFont.getName(), ThemeUtil.HEADER_FONT.getStyle(), emojiFont.getSize()));
        titleLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JComboBox<String> accSelector = new JComboBox<>();
        JComboBox<String> billTypeBox = new JComboBox<>(new String[]{"Electricity", "Water", "Internet"});
        JTextField amountField = new JTextField(20);
        styleTextField(amountField);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(createStyledLabel("Account:"), gbc);
        gbc.gridx = 1;
        panel.add(accSelector, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(createStyledLabel("Bill Type:"), gbc);
        gbc.gridx = 1;
        panel.add(billTypeBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(createStyledLabel("Amount (LKR):"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);

        JButton payBtn = new JButton("💳 Pay Bill");
        styleButton(payBtn);
        payBtn.addActionListener(e -> {
            try {
                String accNum = (String) accSelector.getSelectedItem();
                if (accNum == null) throw new IllegalArgumentException("No account selected");
                
                double amount = Double.parseDouble(amountField.getText().trim());
                String billType = (String) billTypeBox.getSelectedItem();
                
                BankAccount account = accountDAO.getAccountByNumber(accNum);
                billPaymentService.payUtilityBill(account, billType, amount);
                accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());
                
                JOptionPane.showMessageDialog(this,
                    "✅ Bill payment of LKR " + String.format("%.2f", amount) + " completed!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAccounts();
                amountField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(payBtn, gbc);

        return panel;
    }

    private JPanel createLoansPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeUtil.COLOR_BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("📈 Apply for a Loan");
        // Fix emoji display
        Font emojiFont = createEmojiSupportingFont(ThemeUtil.HEADER_FONT.getSize());
        titleLabel.setFont(new Font(emojiFont.getName(), ThemeUtil.HEADER_FONT.getStyle(), emojiFont.getSize()));
        titleLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        JComboBox<LoanType> loanTypeBox = new JComboBox<>(LoanType.values());
        JComboBox<String> accSelector = new JComboBox<>();
        JTextField amountField = new JTextField(20);
        JTextField termField = new JTextField(20);
        styleTextField(amountField);
        styleTextField(termField);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(createStyledLabel("Disbursement Account:"), gbc);
        gbc.gridx = 1;
        panel.add(accSelector, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(createStyledLabel("Loan Type:"), gbc);
        gbc.gridx = 1;
        panel.add(loanTypeBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(createStyledLabel("Loan Amount (LKR):"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(createStyledLabel("Term (Months):"), gbc);
        gbc.gridx = 1;
        panel.add(termField, gbc);

        JButton applyBtn = new JButton("📋 Apply for Loan");
        styleButton(applyBtn);
        applyBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                int term = Integer.parseInt(termField.getText().trim());
                String accNum = (String) accSelector.getSelectedItem();
                LoanType type = (LoanType) loanTypeBox.getSelectedItem();

                if (accNum == null) throw new IllegalArgumentException("No account selected");

                BankAccount account = accountDAO.getAccountByNumber(accNum);
                Loan loan = loanService.applyForLoan(customer, amount, term, type);
                double monthly = loanService.calculateMonthlyRepayment(loan);

                int choice = JOptionPane.showConfirmDialog(this,
                    String.format("Loan Approved!\n\nPrincipal: LKR %.2f\nMonthly: LKR %.2f\n\nAccept?", amount, monthly),
                    "Loan Offer", JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    bankingService.deposit(account, amount);
                    accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());
                    JOptionPane.showMessageDialog(this, "✅ Loan funds disbursed!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshAccounts();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(applyBtn, gbc);

        return panel;
    }

    private JPanel createStatementsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeUtil.COLOR_BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Monthly Statements");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(ThemeUtil.COLOR_BACKGROUND);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        titlePanel.add(titleLabel);
        panel.add(titlePanel, BorderLayout.NORTH);

        // Control panel with modern styling
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        controlPanel.setBackground(ThemeUtil.COLOR_BACKGROUND);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel accLabel = new JLabel("Select Account:");
        accLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        accLabel.setForeground(ThemeUtil.COLOR_TEXT_DARK);
        controlPanel.add(accLabel);

        JComboBox<String> accSelector = new JComboBox<>();
        accSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        accSelector.setBackground(ThemeUtil.COLOR_WHITE);
        accSelector.setForeground(ThemeUtil.COLOR_TEXT_DARK);
        accSelector.setBorder(BorderFactory.createLineBorder(ThemeUtil.COLOR_BORDER, 1));
        accSelector.setPreferredSize(new Dimension(200, 28));
        
        // CRITICAL FIX: Load account data into the ComboBox
        refreshAccountSelectors(accSelector);
        
        controlPanel.add(accSelector);

        // Generate button with modern styling
        JButton generateBtn = new JButton("Generate Statement");
        generateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        generateBtn.setBackground(ThemeUtil.COLOR_PRIMARY);
        generateBtn.setForeground(ThemeUtil.COLOR_WHITE);
        generateBtn.setFocusPainted(false);
        generateBtn.setBorderPainted(false);
        generateBtn.setContentAreaFilled(true);
        generateBtn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        generateBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        generateBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                generateBtn.setBackground(ThemeUtil.COLOR_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                generateBtn.setBackground(ThemeUtil.COLOR_PRIMARY);
            }
        });

        generateBtn.addActionListener(e -> {
            try {
                String accNum = (String) accSelector.getSelectedItem();
                if (accNum == null || accNum.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please select an account", "No Account Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                BankAccount account = accountDAO.getAccountByNumber(accNum);
                List<Transaction> txHistory = transactionDAO.getTransactionsForAccount(accNum);
                for (Transaction tx : txHistory) {
                    account.addTransaction(tx);
                }
                String statement = statementService.generateMonthlyStatement(account);
                
                // Show statement in a dialog with proper formatting
                JDialog stmtDialog = new JDialog(this, "Account Statement - " + accNum, false);
                stmtDialog.setSize(700, 500);
                stmtDialog.setLocationRelativeTo(this);
                
                JTextArea stmtArea = new JTextArea();
                stmtArea.setText(statement);
                stmtArea.setEditable(false);
                stmtArea.setFont(new Font("Consolas", Font.PLAIN, 11));
                stmtArea.setBackground(ThemeUtil.COLOR_WHITE);
                stmtArea.setForeground(ThemeUtil.COLOR_TEXT_DARK);
                stmtArea.setBorder(new EmptyBorder(10, 10, 10, 10));
                stmtArea.setLineWrap(false);
                
                JScrollPane scrollPane = new JScrollPane(stmtArea);
                scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                
                stmtDialog.add(scrollPane);
                stmtDialog.setVisible(true);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        controlPanel.add(generateBtn);
        panel.add(controlPanel, BorderLayout.NORTH);

        return panel;
    }

    // ============= TRANSACTION DIALOGS =============

    private void openDepositDialog() {
        JDialog dialog = new JDialog(this, "💰 DEPOSIT", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JLabel titleLabel = new JLabel("💰 Make a Deposit");
        // Fix emoji display
        Font emojiFont = createEmojiSupportingFont(18);
        titleLabel.setFont(new Font(emojiFont.getName(), Font.BOLD, 18));
        titleLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        JComboBox<String> accSelector = new JComboBox<>();
        refreshAccountSelectors(accSelector);
        JTextField amountField = new JTextField(20);
        styleTextField(amountField);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(createStyledLabel("Account:"), gbc);
        gbc.gridx = 1;
        panel.add(accSelector, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(createStyledLabel("Amount (LKR):"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);
        
        JButton depositBtn = new JButton("💰 DEPOSIT");
        ThemeUtil.styleButton(depositBtn);
        depositBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        depositBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                String accNum = (String) accSelector.getSelectedItem();
                if (accNum == null) throw new IllegalArgumentException("No account selected");
                
                BankAccount account = accountDAO.getAccountByNumber(accNum);
                bankingService.deposit(account, amount);
                accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());
                JOptionPane.showMessageDialog(this, "✅ Deposit of LKR " + String.format("%.2f", amount) + " successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAccounts();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(depositBtn, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void openWithdrawDialog() {
        JDialog dialog = new JDialog(this, "💸 WITHDRAW", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JLabel titleLabel = new JLabel("💸 Make a Withdrawal");
        // Fix emoji display
        Font emojiFont = createEmojiSupportingFont(18);
        titleLabel.setFont(new Font(emojiFont.getName(), Font.BOLD, 18));
        titleLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        JComboBox<String> accSelector = new JComboBox<>();
        refreshAccountSelectors(accSelector);
        JTextField amountField = new JTextField(20);
        styleTextField(amountField);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(createStyledLabel("Account:"), gbc);
        gbc.gridx = 1;
        panel.add(accSelector, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(createStyledLabel("Amount (LKR):"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);
        
        JButton withdrawBtn = new JButton("💸 WITHDRAW");
        ThemeUtil.styleButton(withdrawBtn);
        withdrawBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        withdrawBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                String accNum = (String) accSelector.getSelectedItem();
                if (accNum == null) throw new IllegalArgumentException("No account selected");
                
                BankAccount account = accountDAO.getAccountByNumber(accNum);
                bankingService.withdraw(account, amount);
                accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());
                JOptionPane.showMessageDialog(this, "✅ Withdrawal of LKR " + String.format("%.2f", amount) + " successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAccounts();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(withdrawBtn, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void openTransferDialog() {
        JDialog dialog = new JDialog(this, "🔁 TRANSFER", true);
        dialog.setSize(550, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JLabel titleLabel = new JLabel("🔁 Transfer Funds");
        // Fix emoji display
        Font emojiFont = createEmojiSupportingFont(18);
        titleLabel.setFont(new Font(emojiFont.getName(), Font.BOLD, 18));
        titleLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        JComboBox<String> fromAccSelector = new JComboBox<>();
        JComboBox<String> toAccSelector = new JComboBox<>();
        refreshAccountSelectors(fromAccSelector);
        refreshAccountSelectors(toAccSelector);
        JTextField amountField = new JTextField(20);
        styleTextField(amountField);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(createStyledLabel("From Account:"), gbc);
        gbc.gridx = 1;
        panel.add(fromAccSelector, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(createStyledLabel("To Account:"), gbc);
        gbc.gridx = 1;
        panel.add(toAccSelector, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(createStyledLabel("Amount (LKR):"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);
        
        JButton transferBtn = new JButton("🔁 TRANSFER");
        ThemeUtil.styleButton(transferBtn);
        transferBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        transferBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                String fromAcc = (String) fromAccSelector.getSelectedItem();
                String toAcc = (String) toAccSelector.getSelectedItem();
                if (fromAcc == null || toAcc == null) throw new IllegalArgumentException("Please select both accounts");
                
                BankAccount fromAccount = accountDAO.getAccountByNumber(fromAcc);
                BankAccount toAccount = accountDAO.getAccountByNumber(toAcc);
                bankingService.transfer(fromAccount, toAccount, amount);
                accountDAO.updateBalance(fromAccount.getAccountNumber(), fromAccount.getBalance());
                accountDAO.updateBalance(toAccount.getAccountNumber(), toAccount.getBalance());
                JOptionPane.showMessageDialog(this, "✅ Transfer of LKR " + String.format("%.2f", amount) + " successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAccounts();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(transferBtn, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void openBillPaymentDialog() {
        JDialog dialog = new JDialog(this, "💳 PAY BILL", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JLabel titleLabel = new JLabel("💳 Pay Utility Bill");
        // Fix emoji display
        Font emojiFont = createEmojiSupportingFont(18);
        titleLabel.setFont(new Font(emojiFont.getName(), Font.BOLD, 18));
        titleLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        JComboBox<String> accSelector = new JComboBox<>();
        refreshAccountSelectors(accSelector);
        JComboBox<String> billTypeBox = new JComboBox<>(new String[]{"Electricity", "Water", "Internet"});
        JTextField amountField = new JTextField(20);
        styleTextField(amountField);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(createStyledLabel("Account:"), gbc);
        gbc.gridx = 1;
        panel.add(accSelector, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(createStyledLabel("Bill Type:"), gbc);
        gbc.gridx = 1;
        panel.add(billTypeBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(createStyledLabel("Amount (LKR):"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);
        
        JButton payBtn = new JButton("💳 PAY BILL");
        ThemeUtil.styleButton(payBtn);
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        payBtn.addActionListener(e -> {
            try {
                String accNum = (String) accSelector.getSelectedItem();
                if (accNum == null) throw new IllegalArgumentException("No account selected");
                
                double amount = Double.parseDouble(amountField.getText().trim());
                String billType = (String) billTypeBox.getSelectedItem();
                
                BankAccount account = accountDAO.getAccountByNumber(accNum);
                billPaymentService.payUtilityBill(account, billType, amount);
                accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());
                
                JOptionPane.showMessageDialog(this, "✅ Bill payment of LKR " + String.format("%.2f", amount) + " completed!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAccounts();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(payBtn, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void openLoanDialog() {
        JOptionPane.showMessageDialog(this, "Use the Loans tab to apply for loans.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshAccountSelectors(JComboBox<String> selector) {
        selector.removeAllItems();
        List<BankAccount> accounts = accountDAO.getAccountsForCustomer(customer);
        for (BankAccount acc : accounts) {
            selector.addItem(acc.getAccountNumber());
        }
    }

    // UI Helpers

    /**
     * Creates a modern stat card with rounded corners, shadow, and colored top border
     * Ensures proper centering and spacing
     */
    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background with rounded corners
                g2.setColor(ThemeUtil.COLOR_WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                // Subtle shadow effect
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);
                
                // Colored top border (4px height)
                g2.setColor(accentColor);
                g2.fillRect(0, 0, getWidth(), 4);
                
                super.paintComponent(g);
            }
        };
        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(200, 120));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Title label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLabel.setForeground(accentColor);
        gbc.gridy = 0;
        card.add(titleLabel, gbc);

        // Value label
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(ThemeUtil.COLOR_TEXT_DARK);
        gbc.gridy = 1;
        gbc.insets = new Insets(6, 0, 0, 0);
        card.add(valueLabel, gbc);

        return card;
    }

    /**
     * Creates a modern action card button with clean design (no borders/focus painting)
     * Uses GridBagLayout for perfect centering of icon, title and description
     */
    private JButton createModernActionCard(String icon, String title, String description, 
                                           java.awt.event.ActionListener action) {
        JButton btn = new JButton() {
            private boolean hovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card background with rounded corners (clean design)
                if (hovered) {
                    g2.setColor(new Color(240, 245, 255)); // Light blue on hover
                } else {
                    g2.setColor(ThemeUtil.COLOR_WHITE); // White background
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                // Enhanced shadow effect for more depth
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 10, 10);
                
                // Left border accent (thicker and more vibrant)
                g2.setColor(ThemeUtil.COLOR_ACCENT);
                g2.fillRect(0, 0, 5, getHeight());
                
                // Optional: Add a subtle light gray border for better definition
                g2.setColor(new Color(220, 220, 220));
                g2.setStroke(new java.awt.BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                super.paintComponent(g);
            }
        };

        // Set layout and remove all border/focus painting
        btn.setLayout(new GridBagLayout());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(180, 140));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); // Reduced padding
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3; // Reduced weighty

        // Icon - medium sized, centered
        // Fix emoji display
        JLabel iconLabel = new JLabel(icon);
        Font emojiFont = createEmojiSupportingFont(24); // Smaller icon size
        iconLabel.setFont(emojiFont);
        btn.add(iconLabel, gbc);

        // Vertical spacing
        gbc.gridy = 1;
        gbc.weighty = 0.1;
        btn.add(Box.createVerticalStrut(8), gbc);

        // Title - bold, centered, no wrapping
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        gbc.weighty = 0.25;
        gbc.insets = new Insets(0, 8, 0, 8);
        btn.add(titleLabel, gbc);

        // Description - optional, smaller, gray
        if (description != null && !description.isEmpty()) {
            JLabel descLabel = new JLabel(description);
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            descLabel.setForeground(new Color(120, 120, 120));
            descLabel.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridy = 3;
            gbc.weighty = 0.25;
            gbc.insets = new Insets(2, 8, 0, 8);
            btn.add(descLabel, gbc);
        }

        // Hover effects
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.repaint();
            }
        });

        final boolean[] prevHovered = {false};
        btn.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                boolean currentHovered = btn.contains(e.getPoint());
                if (currentHovered != prevHovered[0]) {
                    prevHovered[0] = currentHovered;
                }
            }
        });

        btn.addActionListener(action);

        return btn;
    }

    /**
     * Creates a Font that supports emoji rendering properly
     * Tries multiple fonts across platforms (Windows, macOS, Linux)
     * Falls back to default if system doesn't have emoji font
     */
    private Font createEmojiSupportingFont(int size) {
        // Get all available fonts on the system
        String[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();
        
        // Try fonts in order of platform compatibility
        String[] emojiFonts = {
            "Segoe UI Emoji",      // Windows (built-in, most reliable)
            "Apple Color Emoji",   // macOS (Apple's emoji font)
            "Noto Color Emoji",    // Linux (Google's open-source emoji font)
            "Segoe UI",            // Windows fallback
            "Dialog"               // Universal fallback
        };
        
        // Check available fonts (case-insensitive)
        for (String fontName : emojiFonts) {
            for (String availableFont : allFonts) {
                if (availableFont.equalsIgnoreCase(fontName)) {
                    return new Font(fontName, Font.PLAIN, size);
                }
            }
        }
        
        // Fallback if none found
        return new Font("Dialog", Font.PLAIN, size);
    }

    private JButton createBoldActionButton(String icon, String label, java.awt.event.ActionListener action) {
        // DEPRECATED: Use createModernActionCard instead
        return createModernActionCard(icon, label, "", action);
    }

    private JButton createActionButton(String icon, String label, java.awt.event.ActionListener action) {
        JButton btn = new JButton("<html><center>" + icon + "<br>" + label + "</center></html>");
        btn.setPreferredSize(new Dimension(120, 100));
        btn.setFont(ThemeUtil.LABEL_FONT);
        btn.setBackground(ThemeUtil.COLOR_LIGHT_BG);
        btn.setForeground(ThemeUtil.COLOR_PRIMARY);
        btn.setBorder(new javax.swing.border.CompoundBorder(
            new javax.swing.border.LineBorder(ThemeUtil.COLOR_ACCENT, 2),
            new EmptyBorder(10, 10, 10, 10)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(ThemeUtil.COLOR_ACCENT);
                btn.setForeground(ThemeUtil.COLOR_PRIMARY);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(ThemeUtil.COLOR_LIGHT_BG);
                btn.setForeground(ThemeUtil.COLOR_PRIMARY);
            }
        });

        return btn;
    }

    /**
     * Styles buttons with modern appearance - NO visible borders or focus painting
     * Clean colors with smooth transitions and hand cursor
     */
    private void styleButton(JButton btn) {
        btn.setBackground(ThemeUtil.COLOR_PRIMARY);
        btn.setForeground(ThemeUtil.COLOR_WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(ThemeUtil.COLOR_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(ThemeUtil.COLOR_PRIMARY);
            }
        });
    }

    /**
     * Styles secondary buttons with outline appearance
     * Uses no borders but proper spacing
     */
    private void styleSecondaryButton(JButton btn) {
        btn.setBackground(ThemeUtil.COLOR_BACKGROUND);
        btn.setForeground(ThemeUtil.COLOR_PRIMARY);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(245, 245, 245));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(ThemeUtil.COLOR_BACKGROUND);
            }
        });
    }

    /**
     * Styles text fields with clean appearance - subtle border only
     */
    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBackground(ThemeUtil.COLOR_WHITE);
        field.setForeground(ThemeUtil.COLOR_TEXT_DARK);
        // Clean border with no focus painting - just a subtle line
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setPreferredSize(new Dimension(0, 35));
    }

    private JLabel createStyledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(ThemeUtil.LABEL_FONT);
        lbl.setForeground(ThemeUtil.COLOR_PRIMARY);
        return lbl;
    }

    private void styleModernTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(ThemeUtil.LABEL_FONT);
        table.setBackground(Color.WHITE);
        table.setForeground(ThemeUtil.COLOR_TEXT_DARK);
        table.setGridColor(ThemeUtil.COLOR_LIGHT_BG);
        table.setSelectionBackground(ThemeUtil.COLOR_PRIMARY);
        table.setSelectionForeground(ThemeUtil.COLOR_ACCENT);
        
        table.getTableHeader().setFont(ThemeUtil.BUTTON_FONT);
        table.getTableHeader().setBackground(ThemeUtil.COLOR_PRIMARY);
        table.getTableHeader().setForeground(ThemeUtil.COLOR_ACCENT);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void showTransactionDialog(String type) {
        // Handled in transaction panel
    }

    private void showBillPaymentDialog() {
        // Handled in bill payment panel
    }

    private void showLoanDialog() {
        // Handled in loans panel
    }

    private void showNewAccountDialog() {
        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        optionsPanel.add(new JLabel("Account Type:"));
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Savings Account", "Checking Account"});
        optionsPanel.add(typeBox);
        optionsPanel.add(new JLabel("Currency:"));
        JComboBox<String> currBox = new JComboBox<>(new String[]{"LKR", "USD", "EUR"});
        optionsPanel.add(currBox);

        int choice = JOptionPane.showConfirmDialog(this, optionsPanel, "Open New Account",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.OK_OPTION) {
            String prefix = typeBox.getSelectedIndex() == 0 ? "SAV-" : "CHK-";
            String accNum = prefix + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            BankAccount newAcc = typeBox.getSelectedIndex() == 0 ?
                new com.rajarata.banking.domain.accounts.SavingsAccount(accNum, customer, 0.0) :
                new com.rajarata.banking.domain.accounts.CheckingAccount(accNum, customer, 0.0, 5000.0);
            newAcc.setCurrency((String) currBox.getSelectedItem());
            accountDAO.createAccount(newAcc);
            JOptionPane.showMessageDialog(this, "✅ Account created: " + accNum, "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshAccounts();
        }
    }

    private void showInterestSimulator() {
        JOptionPane.showMessageDialog(this, "Select an account from the Accounts tab first.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void performTransaction(String type, JTextField amountField, JTextField targetField, JTextField clearField) {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            String selectedAccNum = (String) accountSelector.getSelectedItem();
            if (selectedAccNum == null) throw new IllegalArgumentException("No account selected");

            BankAccount account = accountDAO.getAccountByNumber(selectedAccNum);
            if (account == null) throw new IllegalArgumentException("Account not found");

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
                    BankAccount targetAccount = accountDAO.getAccountByNumber(targetField.getText().trim());
                    if (targetAccount == null) throw new IllegalArgumentException("Target account not found");
                    bankingService.transfer(account, targetAccount, amount);
                    accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());
                    accountDAO.updateBalance(targetAccount.getAccountNumber(), targetAccount.getBalance());
                    JOptionPane.showMessageDialog(this, "✅ Transfer successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    break;
            }
            clearField.setText("");
            refreshAccounts();  // This now calls refreshStats() and refreshHistory() internally
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Transaction failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAccounts() {
        // Clear and rebuild the account table from database
        accountTableModel.setRowCount(0);
        if (accountSelector != null) {
            accountSelector.removeAllItems();
        }

        // Fetch fresh account data from database
        List<BankAccount> accounts = accountDAO.getAccountsForCustomer(customer);
        if (accounts == null) {
            System.err.println("Warning: No accounts returned from database");
            accounts = new java.util.ArrayList<>();
        }
        
        // Populate table and dropdown with current balances
        for (BankAccount acc : accounts) {
            String type = acc instanceof com.rajarata.banking.domain.accounts.CheckingAccount ? "Checking" : "Savings";
            accountTableModel.addRow(new Object[]{
                acc.getAccountNumber(),
                type,
                String.format("%.2f", acc.getBalance()),  // Display current balance from DB
                acc.getCurrency()
            });
            if (accountSelector != null) {
                accountSelector.addItem(acc.getAccountNumber());
            }
        }
        
        // Also refresh stats and transaction history
        refreshStats();
        refreshHistory();
    }

    /**
     * Refresh the statistics panel with updated account data
     */
    private void refreshStats() {
        if (statsPanel == null) return;
        
        // Clear old stat cards
        statsPanel.removeAll();
        
        // Calculate fresh statistics from database
        List<BankAccount> accounts = accountDAO.getAccountsForCustomer(customer);
        int totalAccounts = accounts != null ? accounts.size() : 0;
        
        double totalBalance = 0.0;
        int totalTransactions = 0;
        if (accounts != null) {
            for (BankAccount acc : accounts) {
                totalBalance += acc.getBalance();
                List<Transaction> txList = transactionDAO.getTransactionsForAccount(acc.getAccountNumber());
                if (txList != null) {
                    totalTransactions += txList.size();
                }
            }
        }
        
        // Add updated stat cards
        statsPanel.add(createStatCard("Total Accounts", String.valueOf(totalAccounts), ThemeUtil.COLOR_PRIMARY));
        statsPanel.add(createStatCard("Total Balance", String.format("LKR %.2f", totalBalance), ThemeUtil.COLOR_ACCENT));
        statsPanel.add(createStatCard("Transactions", String.valueOf(totalTransactions), ThemeUtil.COLOR_SUCCESS));
        
        // Revalidate and repaint to show updated stats
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private void refreshHistory() {
        // Safely handle null table model - return silently if not initialized
        if (txTableModel == null) {
            return;
        }
        
        txTableModel.setRowCount(0);
        List<BankAccount> accounts = accountDAO.getAccountsForCustomer(customer);
        for (BankAccount acc : accounts) {
            List<Transaction> txList = transactionDAO.getTransactionsForAccount(acc.getAccountNumber());
            if (txList == null) continue;  // Skip if no transactions found
            
            for (Transaction tx : txList) {
                if (tx != null) {  // Extra null safety check
                    txTableModel.addRow(new Object[]{
                        tx.getTimestamp().toString().replace("T", " "),
                        acc.getAccountNumber(),
                        tx.getType().name(),
                        String.format("%.2f", tx.getAmount()),
                        tx.getStatus().name()
                    });
                }
            }
        }
    }

    public static void main(String[] args) {
        // 1. Establish SQLite DB and tables
        com.rajarata.banking.db.DatabaseManager.initializeDatabase();
        
        // 2. Safely populate default records if none exist
        com.rajarata.banking.db.UserDAO userDAO = new com.rajarata.banking.db.UserDAO();
        if (userDAO.getAllUsers().isEmpty()) {
            System.out.println("Initializing default dataset...");
            
            com.rajarata.banking.domain.users.Administrator admin = new com.rajarata.banking.domain.users.Administrator("A1", "Super Admin", "admin@bank.com", "011", "SA", "IT");
            admin.setPasswordHash(java.util.Base64.getEncoder().encodeToString("admin123".getBytes()));
            userDAO.addUser(admin, "ADMIN");

            com.rajarata.banking.domain.users.Customer cust = new com.rajarata.banking.domain.users.Customer("C1", "Jane Doe", "jane@bank.com", "077", "Colombo", java.time.LocalDate.of(1995, 1, 1));
            cust.setPasswordHash(java.util.Base64.getEncoder().encodeToString("cust123".getBytes()));
            userDAO.addUser(cust, "CUSTOMER");
            
            // 2.5 Populate Default Accounts
            com.rajarata.banking.db.AccountDAO accountDAO = new com.rajarata.banking.db.AccountDAO();
            com.rajarata.banking.domain.accounts.CheckingAccount checkAcc = new com.rajarata.banking.domain.accounts.CheckingAccount("CHK-1001", cust, 5000.0, 5000.0);
            com.rajarata.banking.domain.accounts.SavingsAccount saveAcc = new com.rajarata.banking.domain.accounts.SavingsAccount("SAV-2001", cust, 15000.0);
            accountDAO.createAccount(checkAcc);
            accountDAO.createAccount(saveAcc);

            System.out.println("✅ System initialized successfully.");
        }

        // Get the default customer
        com.rajarata.banking.domain.users.Customer customer = (com.rajarata.banking.domain.users.Customer) userDAO.getUserById("C1");

        // 3. Launch UI
        javax.swing.SwingUtilities.invokeLater(() -> {
            ThemeUtil.applyTheme();
            new CustomerDashboard(customer).setVisible(true);
        });
    }
}
