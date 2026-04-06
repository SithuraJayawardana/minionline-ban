package com.rajarata.banking.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

public class ThemeUtil {
    // Modern Professional Theme - Clean, Premium, Professional
    // Primary (Base): Maroon (#7A0C1E) - Professional confidence
    public static final Color COLOR_PRIMARY      = new Color(0x7A, 0x0C, 0x1E);    // #7A0C1E - Deep maroon
    // Secondary (Action): Gold (#D4AF37) - Premium accent
    public static final Color COLOR_ACCENT       = new Color(0xD4, 0xAF, 0x37);    // #D4AF37 - Gold
    // Background: Light Gray (#F5F5F5) - Clean, modern
    public static final Color COLOR_BACKGROUND   = new Color(0xF5, 0xF5, 0xF5);    // #F5F5F5 - Light gray
    // Text: Dark Gray (#333333) - Readable
    public static final Color COLOR_TEXT_DARK    = new Color(0x33, 0x33, 0x33);    // #333333 - Dark gray
    // White for contrast
    public static final Color COLOR_WHITE        = new Color(0xFF, 0xFF, 0xFF);    // #FFFFFF - White
    
    // Supporting Colors
    public static final Color COLOR_LIGHT_BG     = new Color(0xF9, 0xF9, 0xF9);    // Very light gray
    public static final Color COLOR_BORDER       = new Color(0xCC, 0xCC, 0xCC);    // Light border
    public static final Color COLOR_HOVER        = new Color(0x5F, 0x0A, 0x16);    // Darker maroon for hover
    public static final Color COLOR_SUCCESS      = new Color(0x27, 0xAE, 0x60);    // Green for success
    public static final Color COLOR_ERROR        = new Color(0xE7, 0x4C, 0x3C);    // Red for error
    public static final Color COLOR_WARNING      = new Color(0xF3, 0x97, 0x23);    // Orange for warning
    public static final Color COLOR_CARD_BG      = new Color(0xFF, 0xFF, 0xFF);    // White cards

    public static final Font MAIN_FONT          = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font HEADER_FONT        = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font SUBHEADER_FONT     = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font LABEL_FONT         = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BUTTON_FONT        = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font SMALL_FONT         = new Font("Segoe UI", Font.PLAIN, 11);

    /**
     * Styles a primary action button with the modern theme.
     * Includes rounded corners and hover-ready styling.
     */
    public static void styleButton(JButton btn) {
        btn.setBackground(COLOR_PRIMARY);
        btn.setForeground(COLOR_WHITE);
        Font emojiFont = getEmojiSupportingFont(14);
        btn.setFont(new Font(emojiFont.getName(), Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_PRIMARY, 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static Font getEmojiSupportingFont(int size) {
        String[] emojiFonts = {
            "Segoe UI Emoji",      // Windows (built-in, most reliable)
            "Apple Color Emoji",   // macOS
            "Noto Color Emoji",    // Linux
            "Segoe UI",            // Fallback
            "Dialog"               // Universal fallback
        };
        
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();
        
        for (String fontName : emojiFonts) {
            for (String available : availableFonts) {
                if (available.equalsIgnoreCase(fontName)) {
                    return new Font(fontName, Font.PLAIN, size);
                }
            }
        }
        return new Font("Dialog", Font.PLAIN, size);
    }

    /**
     * Styles a secondary action button with outline style.
     */
    public static void styleSecondaryButton(JButton btn) {
        btn.setBackground(COLOR_BACKGROUND);
        btn.setForeground(COLOR_PRIMARY);
        btn.setFont(BUTTON_FONT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_PRIMARY, 2),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Applies uniform colors globally to the Swing UI Manager.
     */
    public static void applyTheme() {
        UIManager.put("Panel.background",           COLOR_BACKGROUND);
        UIManager.put("Label.foreground",           COLOR_TEXT_DARK);
        UIManager.put("Label.font",                 MAIN_FONT);
        UIManager.put("Button.background",          COLOR_PRIMARY);
        UIManager.put("Button.foreground",          COLOR_ACCENT);
        UIManager.put("Button.font",                BUTTON_FONT);
        UIManager.put("TextField.background",       Color.WHITE);
        UIManager.put("TextField.foreground",       COLOR_TEXT_DARK);
        UIManager.put("TextField.border",           BorderFactory.createLineBorder(COLOR_BORDER, 1));
        UIManager.put("PasswordField.background",   Color.WHITE);
        UIManager.put("PasswordField.foreground",   COLOR_TEXT_DARK);
        UIManager.put("PasswordField.border",       BorderFactory.createLineBorder(COLOR_BORDER, 1));
        UIManager.put("TabbedPane.background",      COLOR_BACKGROUND);
        UIManager.put("TabbedPane.foreground",      COLOR_TEXT_DARK);
        UIManager.put("TabbedPane.selected",        COLOR_PRIMARY);
        UIManager.put("TabbedPane.unselectedBackground", COLOR_LIGHT_BG);
        UIManager.put("Table.background",           Color.WHITE);
        UIManager.put("Table.foreground",           COLOR_TEXT_DARK);
        UIManager.put("Table.gridColor",            COLOR_LIGHT_BG);
        UIManager.put("TableHeader.background",     COLOR_PRIMARY);
        UIManager.put("TableHeader.foreground",     COLOR_ACCENT);
        UIManager.put("ComboBox.background",        Color.WHITE);
        UIManager.put("ComboBox.foreground",        COLOR_TEXT_DARK);
    }
    public static final String LOGO_FILE_NAME = "Rajarata_logo.png";

    public static ImageIcon loadLogoIcon(int width, int height) {
        try {
            URL resourceUrl = ThemeUtil.class.getResource("/" + LOGO_FILE_NAME);
            if (resourceUrl != null) {
                ImageIcon icon = new ImageIcon(resourceUrl);
                Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(image);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Font createEmojiSupportingFont(int size) {
        return new Font("Segoe UI Emoji", Font.PLAIN, size);
    }}
