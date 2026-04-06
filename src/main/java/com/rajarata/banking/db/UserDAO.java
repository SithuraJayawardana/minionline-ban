package com.rajarata.banking.db;

import com.rajarata.banking.domain.users.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    public void addUser(User user, String role) {
        String query = "INSERT INTO users (user_id, name, email, phone_number, role, password_hash) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPhoneNumber());
            pstmt.setString(5, role);
            pstmt.setString(6, user.getPasswordHash());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
        }
    }

    public void removeUser(String userId) {
        String query = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing user: " + e.getMessage());
        }
    }

    public User authenticate(String email, String hashedPassword) {
        String query = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.setString(2, hashedPassword);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    String id = rs.getString("user_id");
                    String name = rs.getString("name");
                    String pEmail = rs.getString("email");
                    String phone = rs.getString("phone_number");

                    User u;
                    if (role.equalsIgnoreCase("ADMIN")) {
                        u = new Administrator(id, name, pEmail, phone, "SystemAdmin", "IT");
                    } else if (role.equalsIgnoreCase("STAFF")) {
                        u = new BankStaff(id, name, pEmail, phone, "E" + id, "BR1");
                    } else {
                        u = new Customer(id, name, pEmail, phone, "Sample Address", java.time.LocalDate.now());
                    }
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setLocked(rs.getBoolean("is_locked"));
                    u.setFailedLoginAttempts(rs.getInt("failed_attempts"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }

    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    String id = rs.getString("user_id");
                    String name = rs.getString("name");
                    String pEmail = rs.getString("email");
                    String phone = rs.getString("phone_number");

                    User u;
                    if (role.equalsIgnoreCase("ADMIN")) {
                        u = new Administrator(id, name, pEmail, phone, "SystemAdmin", "IT");
                    } else if (role.equalsIgnoreCase("STAFF")) {
                        u = new BankStaff(id, name, pEmail, phone, "E" + id, "BR1");
                    } else {
                        u = new Customer(id, name, pEmail, phone, "Sample Address", java.time.LocalDate.now());
                    }
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setLocked(rs.getBoolean("is_locked"));
                    u.setFailedLoginAttempts(rs.getInt("failed_attempts"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by email: " + e.getMessage());
        }
        return null;
    }

    public void incrementFailedAttempts(String email) {
        String query = "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error incrementing: " + e.getMessage());
        }
    }

    public void lockUser(String email) {
        String query = "UPDATE users SET is_locked = 1 WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error locking: " + e.getMessage());
        }
    }

    public void resetFailedAttempts(String email) {
        String query = "UPDATE users SET failed_attempts = 0 WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error resetting: " + e.getMessage());
        }
    }

    public int getFailedAttempts(String email) {
        String query = "SELECT failed_attempts FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("failed_attempts");
            }
        } catch (SQLException e) {}
        return 0;
    }

    public boolean isLocked(String email) {
        String query = "SELECT is_locked FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getBoolean("is_locked");
            }
        } catch (SQLException e) {}
        return false;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                // Return generic users for list view
                String role = rs.getString("role");
                if(role.equalsIgnoreCase("ADMIN")) {
                   list.add(new Administrator(rs.getString("user_id"), rs.getString("name"), rs.getString("email"), rs.getString("phone_number"), "Admin", "AdminDept"));
                } else if(role.equalsIgnoreCase("STAFF")) {
                   list.add(new BankStaff(rs.getString("user_id"), rs.getString("name"), rs.getString("email"), rs.getString("phone_number"), "EID", "BR"));
                } else {
                   list.add(new Customer(rs.getString("user_id"), rs.getString("name"), rs.getString("email"), rs.getString("phone_number"), "Address", java.time.LocalDate.now()));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listing users: " + e.getMessage());
        }
        return list;
    }
}
