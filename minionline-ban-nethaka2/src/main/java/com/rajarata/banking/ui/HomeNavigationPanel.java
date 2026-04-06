package com.rajarata.banking.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified navigation panel for banking application.
 * Provides a modern, mobile-friendly grid layout for all banking functions.
 */
public class HomeNavigationPanel extends JPanel {
    private List<NavigationButton> buttons = new ArrayList<>();

    public HomeNavigationPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeUtil.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));
    }

    /**
     * Create the main navigation grid with header and menu items
     */
    public void buildNavigationGrid(String userName, int columnCount) {
        removeAll();

        // Header Panel
        JPanel headerPanel = createHeaderPanel(userName);
        add(headerPanel, BorderLayout.NORTH);

        // Navigation Grid
        JPanel gridPanel = createGridPanel(columnCount);
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBackground(ThemeUtil.COLOR_BACKGROUND);
        scrollPane.getViewport().setBackground(ThemeUtil.COLOR_BACKGROUND);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createHeaderPanel(String userName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeUtil.COLOR_PRIMARY);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome back, " + userName);
        welcomeLabel.setFont(ThemeUtil.HEADER_FONT);
        welcomeLabel.setForeground(ThemeUtil.COLOR_ACCENT);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(welcomeLabel);

        // Subtitle
        JLabel subtitleLabel = new JLabel("What would you like to do today?");
        subtitleLabel.setFont(ThemeUtil.SUBHEADER_FONT);
        subtitleLabel.setForeground(new Color(220, 220, 220));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitleLabel);

        return panel;
    }

    private JPanel createGridPanel(int columnCount) {
        JPanel gridPanel = new JPanel(new GridLayout(0, columnCount, 15, 15));
        gridPanel.setBackground(ThemeUtil.COLOR_BACKGROUND);
        gridPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        for (NavigationButton btn : buttons) {
            JPanel cardPanel = createNavigationCard(btn);
            gridPanel.add(cardPanel);
        }

        return gridPanel;
    }

    private JPanel createNavigationCard(NavigationButton btn) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(ThemeUtil.COLOR_LIGHT_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                g2.setColor(ThemeUtil.COLOR_ACCENT);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(200, 180));
        card.setMaximumSize(new Dimension(200, 180));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Icon/Emoji
        JLabel iconLabel = new JLabel(btn.getIcon());
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLabel);

        card.add(Box.createVerticalStrut(10));

        // Title
        JLabel titleLabel = new JLabel(btn.getTitle());
        titleLabel.setFont(ThemeUtil.SUBHEADER_FONT);
        titleLabel.setForeground(ThemeUtil.COLOR_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);

        // Description
        if (btn.getDescription() != null && !btn.getDescription().isEmpty()) {
            JLabel descLabel = new JLabel("<html><center>" + btn.getDescription() + "</center></html>");
            descLabel.setFont(ThemeUtil.SMALL_FONT);
            descLabel.setForeground(new Color(100, 100, 100));
            descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(Box.createVerticalStrut(5));
            card.add(descLabel);
        }

        // Make card clickable
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                btn.getActionListener().actionPerformed(null);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(ThemeUtil.COLOR_ACCENT);
                card.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(ThemeUtil.COLOR_LIGHT_BG);
                card.repaint();
            }
        });

        return card;
    }

    /**
     * Add a navigation button to the grid
     */
    public void addNavigationButton(String icon, String title, String description, ActionListener action) {
        buttons.add(new NavigationButton(icon, title, description, action));
    }

    /**
     * Clear all navigation buttons
     */
    public void clearButtons() {
        buttons.clear();
    }

    /**
     * Inner class to represent a navigation button
     */
    private static class NavigationButton {
        private String icon;
        private String title;
        private String description;
        private ActionListener actionListener;

        public NavigationButton(String icon, String title, String description, ActionListener actionListener) {
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.actionListener = actionListener;
        }

        public String getIcon() {
            return icon;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public ActionListener getActionListener() {
            return actionListener;
        }
    }
}
