package com.rajarata.banking.ui;

import com.rajarata.banking.db.UserDAO;
import com.rajarata.banking.domain.users.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.RenderingHints;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private UserDAO userDAO;

    public LoginFrame(UserDAO userDAO) {
        this.userDAO = userDAO;
        setTitle("Rajarata Digital Bank - Secure Login");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Create main content panel with two sections
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 0, 0));
        mainContent.setBackground(ThemeUtil.COLOR_PRIMARY);

        // Left panel - Logo and branding
        JPanel leftPanel = createBrandingPanel();
        
        // Right panel - Login form
        JPanel rightPanel = createLoginPanel();
        
        mainContent.add(leftPanel);
        mainContent.add(rightPanel);

        add(mainContent);
    }

    private JPanel createBrandingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(ThemeUtil.COLOR_PRIMARY);
        
        // Centered content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ThemeUtil.COLOR_PRIMARY);
        contentPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        ImageIcon logoIcon = ThemeUtil.loadLogoIcon(180, 180);
        if (logoIcon != null) {
            JLabel logoLabel = new JLabel(logoIcon);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(logoLabel);
        } else {
            JLabel logoPlaceholder = new JLabel("🏦");
            Font emojiFont = createEmojiSupportingFont(80);
            logoPlaceholder.setFont(emojiFont);
            logoPlaceholder.setForeground(ThemeUtil.COLOR_ACCENT);
            logoPlaceholder.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(logoPlaceholder);
        }

        contentPanel.add(Box.createVerticalStrut(15));

        // Bank name
        JLabel bankName = new JLabel("RAJARATA");
        bankName.setFont(new Font("Segoe UI", Font.BOLD, 36));
        bankName.setForeground(ThemeUtil.COLOR_ACCENT);
        bankName.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(bankName);

        // Bank tagline
        JLabel bankTagline = new JLabel("Digital Banking Solutions");
        bankTagline.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        bankTagline.setForeground(new Color(200, 200, 200));
        bankTagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(bankTagline);

        // Divider
        contentPanel.add(Box.createVerticalStrut(30));
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(255, 193, 7, 80));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        separator.setMaximumSize(new Dimension(250, 1));
        contentPanel.add(separator);

        

        contentPanel.add(Box.createVerticalGlue());
        
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLoginPanel() {
        // Outer panel with padding
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(ThemeUtil.COLOR_BACKGROUND);
        
        // Create card container with shadow effect
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);
                
                // Draw white card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                
                super.paintComponent(g);
            }
        };
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(new EmptyBorder(45, 50, 45, 50));
        cardPanel.setMaximumSize(new Dimension(450, Integer.MAX_VALUE));

        // Title
        JLabel loginTitle = new JLabel("Secure Login");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        loginTitle.setForeground(ThemeUtil.COLOR_PRIMARY);
        loginTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(loginTitle);
        
        // Subtitle
        JLabel subtitle = new JLabel("Enter your credentials to access your account");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(102, 102, 102));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(subtitle);
        
        cardPanel.add(Box.createVerticalStrut(30));

        // Email field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(emailLabel);
        cardPanel.add(Box.createVerticalStrut(6));
        
        emailField = new JTextField();
        styleTextField(emailField);
        emailField.setMaximumSize(new Dimension(350, 38));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(emailField);
        cardPanel.add(Box.createVerticalStrut(18));

        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(passwordLabel);
        cardPanel.add(Box.createVerticalStrut(6));
        
        passwordField = new JPasswordField();
        styleTextField(passwordField);
        passwordField.setMaximumSize(new Dimension(350, 38));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(passwordField);
        cardPanel.add(Box.createVerticalStrut(24));

        // Login button
        JButton loginButton = new JButton("Sign In");
        loginButton.setBackground(ThemeUtil.COLOR_PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setMaximumSize(new Dimension(350, 40));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add hover effect
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                loginButton.setBackground(new Color(95, 12, 30));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                loginButton.setBackground(ThemeUtil.COLOR_PRIMARY);
            }
        });
        
        loginButton.addActionListener(this::handleLogin);
        cardPanel.add(loginButton);
        
        cardPanel.add(Box.createVerticalStrut(12));

        // Demo credentials hint
        JLabel hintLabel = new JLabel("<html><span style='font-size:11px; color:#888;'>Demo: admin@rajarata.com / password: password</span></html>");
        hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(hintLabel);
        
        cardPanel.add(Box.createVerticalStrut(18));
        
        // Divider
        JSeparator divider = new JSeparator(JSeparator.HORIZONTAL);
        divider.setMaximumSize(new Dimension(350, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        divider.setForeground(new Color(220, 220, 220));
        cardPanel.add(divider);

        cardPanel.add(Box.createVerticalStrut(18));

        // Sign up link
        JButton signUpButton = new JButton("Create New Account");
        signUpButton.setBackground(Color.WHITE);
        signUpButton.setForeground(ThemeUtil.COLOR_PRIMARY);
        signUpButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        signUpButton.setFocusPainted(false);
        signUpButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.COLOR_PRIMARY, 2),
            BorderFactory.createEmptyBorder(8, 0, 8, 0)
        ));
        signUpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpButton.setMaximumSize(new Dimension(350, 40));
        signUpButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add hover effect
        signUpButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                signUpButton.setBackground(new Color(245, 245, 245));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                signUpButton.setBackground(Color.WHITE);
            }
        });
        
        signUpButton.addActionListener(e -> {
            new SignUpFrame(userDAO).setVisible(true);
            dispose();
        });
        cardPanel.add(signUpButton);

        // Add card to outer panel with GridBagConstraints for vertical centering
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(20, 30, 20, 30);
        
        outerPanel.add(cardPanel, gbc);
        
        return outerPanel;
    }

    private void styleTextField(JComponent field) {
        if (field instanceof JTextField) {
            ((JTextField) field).setPreferredSize(new Dimension(400, 35));
            ((JTextField) field).setMargin(new Insets(2, 5, 2, 5));
            ((JTextField) field).setCaretColor(ThemeUtil.COLOR_PRIMARY);
        }
        if (field instanceof JPasswordField) {
            ((JPasswordField) field).setPreferredSize(new Dimension(400, 35));
            ((JPasswordField) field).setMargin(new Insets(2, 5, 2, 5));
            ((JPasswordField) field).setCaretColor(ThemeUtil.COLOR_PRIMARY);
        }
        field.setBackground(ThemeUtil.COLOR_WHITE);
        field.setForeground(ThemeUtil.COLOR_TEXT_DARK);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.COLOR_ACCENT, 2),
            BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        field.setFont(ThemeUtil.MAIN_FONT);
        
        // Add focus state styling
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // Change border color to primary on focus
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeUtil.COLOR_PRIMARY, 2),
                    BorderFactory.createEmptyBorder(7, 12, 7, 12)
                ));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                // Revert to accent color
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeUtil.COLOR_ACCENT, 2),
                    BorderFactory.createEmptyBorder(7, 12, 7, 12)
                ));
            }
        });
    }

    private void handleLogin(ActionEvent e) {
        String email = emailField.getText().trim();
        String pwd = new String(passwordField.getPassword());
        
        if (email.isEmpty() || pwd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (userDAO.isLocked(email)) {
            JOptionPane.showMessageDialog(this, 
                "Account locked due to multiple failed login attempts.\nPlease contact an administrator.", 
                "Account Locked", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Simple hash check
        String hashed = java.util.Base64.getEncoder().encodeToString(pwd.getBytes());
        User user = userDAO.authenticate(email, hashed);

        if (user != null) {
            userDAO.resetFailedAttempts(email);
            // Route user to appropriate dashboard
            if (user instanceof Administrator) {
                new AdminDashboard((Administrator) user, userDAO).setVisible(true);
            } else {
                new CustomerDashboard((Customer) user).setVisible(true);
            }
            dispose();
        } else {
            userDAO.incrementFailedAttempts(email);
            int attempts = userDAO.getFailedAttempts(email);
            if (attempts >= 3) {
                userDAO.lockUser(email);
                new com.rajarata.banking.domain.security.AuditLogger()
                    .logEvent("FRAUD_ALERT", "Brute force login detected for email: " + email);
                JOptionPane.showMessageDialog(this, 
                    "Account locked due to 3 failed attempts.\nPlease contact support.", 
                    "Security Alert", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Invalid credentials. Attempt " + attempts + " of 3.", 
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Creates a Font that supports emoji rendering on the current platform.
     * Attempts to use platform-specific fonts in order of preference.
     * 
     * @param size Font size in points
     * @return A Font that can properly render emoji characters
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
}
