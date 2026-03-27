package com.rajarata.banking.domain.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class AuditLogger {
    private static final String LOG_FILE = "security_audit.txt";

    public void logEvent(String eventType, String detail) {
        Path path = Paths.get(LOG_FILE);
        String logEntry = String.format("[%s] [%s] %s%n", LocalDateTime.now(), eventType, detail);
        
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            Files.writeString(path, logEntry, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write to audit log: " + e.getMessage());
        }
    }
}
