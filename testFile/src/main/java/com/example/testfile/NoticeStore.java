package com.example.testfile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class NoticeStore {

    private static final Path NOTICE_FILE = AppDataPaths.resolve("notices.txt");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private NoticeStore() {
    }

    public static synchronized void addNotice(String teacher, String title, String message, int durationDays) 
            throws IOException {
        ensureFile();
        
        LocalDate createdDate = LocalDate.now();
        LocalDate expiryDate = createdDate.plusDays(durationDays);
        
        String sanitizedTitle = title.replace("\t", " ").replace("\n", " ").trim();
        String sanitizedMessage = message.replace("\t", " ").replace("\n", "||").trim();
        
        // Format: teacher|title|message|createdDate|expiryDate|notified
        String line = teacher + "\t" + sanitizedTitle + "\t" + sanitizedMessage + "\t" + 
                createdDate.format(DATE_FORMATTER) + "\t" + expiryDate.format(DATE_FORMATTER) + "\t" + "false" + 
                System.lineSeparator();
        
        Files.writeString(NOTICE_FILE, line, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static synchronized List<Notice> getAllActiveNotices() throws IOException {
        ensureFile();
        List<Notice> notices = new ArrayList<>();
        List<String> lines = Files.readAllLines(NOTICE_FILE, StandardCharsets.UTF_8);
        LocalDate today = LocalDate.now();
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 5) continue;
            
            LocalDate expiryDate = LocalDate.parse(parts[4], DATE_FORMATTER);
            
            // Only include non-expired notices
            if (!expiryDate.isBefore(today)) {
                String message = parts[2].replace("||", "\n");
                boolean notified = parts.length >= 6 && "true".equalsIgnoreCase(parts[5]);
                notices.add(new Notice(parts[0], parts[1], message, 
                        LocalDate.parse(parts[3], DATE_FORMATTER), expiryDate, notified));
            }
        }
        
        // Sort by created date descending (newest first)
        notices.sort(Comparator.comparing(Notice::createdDate).reversed());
        return notices;
    }

    public static synchronized List<Notice> getNoticesByTeacher(String teacher) throws IOException {
        ensureFile();
        List<Notice> notices = new ArrayList<>();
        List<String> lines = Files.readAllLines(NOTICE_FILE, StandardCharsets.UTF_8);
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 5) continue;
            
            if (parts[0].equalsIgnoreCase(teacher)) {
                String message = parts[2].replace("||", "\n");
                boolean notified = parts.length >= 6 && "true".equalsIgnoreCase(parts[5]);
                notices.add(new Notice(parts[0], parts[1], message, 
                        LocalDate.parse(parts[3], DATE_FORMATTER), 
                        LocalDate.parse(parts[4], DATE_FORMATTER), notified));
            }
        }
        
        notices.sort(Comparator.comparing(Notice::createdDate).reversed());
        return notices;
    }

    public static synchronized List<Notice> getExpiringNotices(String teacher) throws IOException {
        ensureFile();
        List<Notice> expiring = new ArrayList<>();
        List<String> lines = Files.readAllLines(NOTICE_FILE, StandardCharsets.UTF_8);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 5) continue;
            
            if (parts[0].equalsIgnoreCase(teacher)) {
                LocalDate expiryDate = LocalDate.parse(parts[4], DATE_FORMATTER);
                boolean notified = parts.length >= 6 && "true".equalsIgnoreCase(parts[5]);
                
                // Check if expiring tomorrow and not yet notified
                if (expiryDate.equals(tomorrow) && !notified) {
                    String message = parts[2].replace("||", "\n");
                    expiring.add(new Notice(parts[0], parts[1], message, 
                            LocalDate.parse(parts[3], DATE_FORMATTER), expiryDate, notified));
                }
            }
        }
        
        return expiring;
    }

    public static synchronized void markNotified(String teacher, String title, LocalDate expiryDate) 
            throws IOException {
        ensureFile();
        List<String> lines = Files.readAllLines(NOTICE_FILE, StandardCharsets.UTF_8);
        StringBuilder updated = new StringBuilder();
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 5) {
                updated.append(line).append(System.lineSeparator());
                continue;
            }
            
            if (parts[0].equalsIgnoreCase(teacher) && parts[1].equals(title) && 
                parts[4].equals(expiryDate.format(DATE_FORMATTER))) {
                // Mark as notified
                updated.append(parts[0]).append("\t")
                       .append(parts[1]).append("\t")
                       .append(parts[2]).append("\t")
                       .append(parts[3]).append("\t")
                       .append(parts[4]).append("\t")
                       .append("true")
                       .append(System.lineSeparator());
            } else {
                updated.append(line).append(System.lineSeparator());
            }
        }
        
        Files.writeString(NOTICE_FILE, updated.toString(), StandardCharsets.UTF_8);
    }

    public static synchronized boolean deleteNotice(String teacher, String title, LocalDate createdDate) 
            throws IOException {
        ensureFile();
        List<String> lines = Files.readAllLines(NOTICE_FILE, StandardCharsets.UTF_8);
        StringBuilder updated = new StringBuilder();
        boolean deleted = false;
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 5) {
                updated.append(line).append(System.lineSeparator());
                continue;
            }
            
            if (parts[0].equalsIgnoreCase(teacher) && parts[1].equals(title) && 
                parts[3].equals(createdDate.format(DATE_FORMATTER))) {
                deleted = true;
                // Skip this line (delete)
            } else {
                updated.append(line).append(System.lineSeparator());
            }
        }
        
        if (deleted) {
            Files.writeString(NOTICE_FILE, updated.toString(), StandardCharsets.UTF_8);
        }
        
        return deleted;
    }

    public static synchronized boolean extendNotice(String teacher, String title, LocalDate createdDate, int additionalDays) 
            throws IOException {
        ensureFile();
        List<String> lines = Files.readAllLines(NOTICE_FILE, StandardCharsets.UTF_8);
        StringBuilder updated = new StringBuilder();
        boolean extended = false;
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 5) {
                updated.append(line).append(System.lineSeparator());
                continue;
            }
            
            if (parts[0].equalsIgnoreCase(teacher) && parts[1].equals(title) && 
                parts[3].equals(createdDate.format(DATE_FORMATTER))) {
                // Extend expiry date
                LocalDate currentExpiry = LocalDate.parse(parts[4], DATE_FORMATTER);
                LocalDate newExpiry = currentExpiry.plusDays(additionalDays);
                
                updated.append(parts[0]).append("\t")
                       .append(parts[1]).append("\t")
                       .append(parts[2]).append("\t")
                       .append(parts[3]).append("\t")
                       .append(newExpiry.format(DATE_FORMATTER)).append("\t")
                       .append("false") // Reset notified flag
                       .append(System.lineSeparator());
                extended = true;
            } else {
                updated.append(line).append(System.lineSeparator());
            }
        }
        
        if (extended) {
            Files.writeString(NOTICE_FILE, updated.toString(), StandardCharsets.UTF_8);
        }
        
        return extended;
    }

    public static synchronized void cleanExpiredNotices() throws IOException {
        ensureFile();
        List<String> lines = Files.readAllLines(NOTICE_FILE, StandardCharsets.UTF_8);
        StringBuilder updated = new StringBuilder();
        LocalDate today = LocalDate.now();
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 5) continue;
            
            LocalDate expiryDate = LocalDate.parse(parts[4], DATE_FORMATTER);
            
            // Keep only non-expired notices
            if (!expiryDate.isBefore(today)) {
                updated.append(line).append(System.lineSeparator());
            }
        }
        
        Files.writeString(NOTICE_FILE, updated.toString(), StandardCharsets.UTF_8);
    }

    public static long getDaysRemaining(Notice notice) {
        return ChronoUnit.DAYS.between(LocalDate.now(), notice.expiryDate());
    }

    private static void ensureFile() throws IOException {
        Path parent = NOTICE_FILE.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(NOTICE_FILE)) {
            Files.createFile(NOTICE_FILE);
        }
    }

    public record Notice(String teacher, String title, String message, 
                         LocalDate createdDate, LocalDate expiryDate, boolean notified) {
    }
}
