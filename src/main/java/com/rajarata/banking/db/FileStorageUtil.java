package com.rajarata.banking.db;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileStorageUtil {
    private static final String LOANS_FILE = "loans_db.txt";

    public static void saveLoan(String loanId, String customerId, double amount, int termMonths, String type) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOANS_FILE, true))) {
            writer.println(loanId + "," + customerId + "," + amount + "," + termMonths + "," + type + "," + java.time.LocalDate.now());
        } catch (IOException e) {
            System.err.println("Error saving loan to file: " + e.getMessage());
        }
    }

    public static List<String> readAllLoans() {
        List<String> loans = new ArrayList<>();
        File file = new File(LOANS_FILE);
        if (!file.exists()) return loans;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    loans.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading loans from file: " + e.getMessage());
        }
        return loans;
    }
}
