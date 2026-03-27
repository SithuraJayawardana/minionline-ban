package com.rajarata.banking.ui;

import javax.swing.*;
import java.awt.*;

public class ThemeUtil {
    // Requested Application Theme: Maroon, Brown, Yellow
    public static final Color COLOR_MAROON       = new Color(128, 0, 0);    // #800000
    public static final Color COLOR_BROWN        = new Color(92, 64, 51);   // #5C4033
    public static final Color COLOR_YELLOW       = new Color(255, 215, 0);  // #FFD700
    public static final Color COLOR_LIGHT_YELLOW = new Color(255, 235, 100);
    public static final Color COLOR_TEXT_LIGHT   = Color.WHITE;
    public static final Color COLOR_BG_PANEL     = new Color(74, 46, 35);   // Slightly lighter brown

    public static final Font MAIN_FONT   = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font LABEL_FONT  = new Font("Segoe UI", Font.PLAIN, 13);

    /**
     * Styles a button with the Rajarata maroon/yellow theme.
     */
    public static void styleButton(JButton btn) {
        btn.setBackground(COLOR_MAROON);
        btn.setForeground(COLOR_YELLOW);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_YELLOW, 1),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Applies uniform colors globally to the Swing UI Manager.
     */
    public static void applyTheme() {
        UIManager.put("Panel.background",      COLOR_BROWN);
        UIManager.put("Label.foreground",      COLOR_TEXT_LIGHT);
        UIManager.put("Label.font",            MAIN_FONT);
        UIManager.put("Button.background",     COLOR_MAROON);
        UIManager.put("Button.foreground",     COLOR_YELLOW);
        UIManager.put("Button.font",           new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("TextField.background",  Color.WHITE);
        UIManager.put("TextField.foreground",  Color.BLACK);
        UIManager.put("PasswordField.background", Color.WHITE);
        UIManager.put("PasswordField.foreground", Color.BLACK);
        UIManager.put("TabbedPane.background", COLOR_MAROON);
        UIManager.put("TabbedPane.foreground", COLOR_YELLOW);
        UIManager.put("TabbedPane.selected",   COLOR_BROWN);
        UIManager.put("Table.background",      Color.WHITE);
        UIManager.put("Table.foreground",      Color.BLACK);
        UIManager.put("Table.gridColor",       COLOR_BROWN);
        UIManager.put("TableHeader.background", COLOR_MAROON);
        UIManager.put("TableHeader.foreground", COLOR_YELLOW);
    }
}
