package com.example.testfile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public final class AuthStore {

    private static final Path STUDENT_FILE = AppDataPaths.resolve("student_accounts.txt");
    private static final Path TEACHER_FILE = AppDataPaths.resolve("teacher_accounts.txt");

    private AuthStore() {
    }

    /**
     * Signup with username, email, and password.
     * Format stored: username,email,password
     */
    public static synchronized boolean signup(String role, String displayName, String email, String password)
            throws IOException {
        Path file = fileForRole(role);
        ensureFile(file);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (String line : lines) {
            String[] parts = line.split(",", 3);
            if (parts.length >= 2) {
                // Check if username or email already exists
                if (parts[0].equals(displayName) || parts[1].equals(email)) {
                    return false;
                }
            }
            // Support old format (username,password) - check username only
            if (parts.length == 2 && parts[0].equals(displayName)) {
                return false;
            }
        }

        String record = displayName + "," + email + "," + password + System.lineSeparator();
        Files.writeString(file, record, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return true;
    }

    /**
     * Login using either username or email.
     * Returns the display name if login is successful, null otherwise.
     */
    public static synchronized String loginAndGetDisplayName(String role, String usernameOrEmail, String password)
            throws IOException {
        Path file = fileForRole(role);
        ensureFile(file);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (String line : lines) {
            String[] parts = line.split(",", 3);
            // New format: username,email,password
            if (parts.length == 3) {
                String storedUsername = parts[0];
                String storedEmail = parts[1];
                String storedPassword = parts[2];
                // Match by username or email
                if ((storedUsername.equals(usernameOrEmail) || storedEmail.equals(usernameOrEmail)) 
                        && storedPassword.equals(password)) {
                    return storedUsername; // Return the display name
                }
            }
            // Old format support: username,password
            if (parts.length == 2 && parts[0].equals(usernameOrEmail) && parts[1].equals(password)) {
                return parts[0]; // Return the username itself
            }
        }
        return null;
    }

    /**
     * Check if login credentials match (for backward compatibility).
     */
    public static synchronized boolean loginMatches(String role, String usernameOrEmail, String password)
            throws IOException {
        return loginAndGetDisplayName(role, usernameOrEmail, password) != null;
    }

    private static Path fileForRole(String role) {
        if ("teacher".equalsIgnoreCase(role)) {
            return TEACHER_FILE;
        }
        return STUDENT_FILE;
    }

    private static void ensureFile(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
    }
}
