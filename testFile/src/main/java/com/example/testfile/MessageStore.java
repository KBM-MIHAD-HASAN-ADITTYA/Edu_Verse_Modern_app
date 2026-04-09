package com.example.testfile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MessageStore {

    private static final Path MESSAGE_FILE = AppDataPaths.resolve("messages.txt");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private MessageStore() {
    }

    public static synchronized void sendMessage(String from, String to, String message, boolean isFromStudent) 
            throws IOException {
        ensureFile();
        
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String sanitizedMessage = message.replace("\t", " ").replace("\n", " ").trim();
        
        // Format: from|to|message|timestamp|isFromStudent|isRead
        String line = from + "\t" + to + "\t" + sanitizedMessage + "\t" + timestamp + "\t" + isFromStudent + "\t" + "false" + System.lineSeparator();
        
        Files.writeString(MESSAGE_FILE, line, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static synchronized List<ChatMessage> getConversation(String user1, String user2) throws IOException {
        ensureFile();
        List<ChatMessage> messages = new ArrayList<>();
        List<String> lines = Files.readAllLines(MESSAGE_FILE, StandardCharsets.UTF_8);
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 5) continue;
            
            String from = parts[0];
            String to = parts[1];
            
            // Check if this message is part of the conversation
            if ((from.equalsIgnoreCase(user1) && to.equalsIgnoreCase(user2)) ||
                (from.equalsIgnoreCase(user2) && to.equalsIgnoreCase(user1))) {
                
                boolean isRead = parts.length >= 6 && "true".equalsIgnoreCase(parts[5]);
                messages.add(new ChatMessage(from, to, parts[2], parts[3], 
                        Boolean.parseBoolean(parts[4]), isRead));
            }
        }
        
        messages.sort(Comparator.comparing(ChatMessage::timestamp));
        return messages;
    }

    public static synchronized List<String> getTeacherList() throws IOException {
        Path teacherFile = AppDataPaths.resolve("teacher_accounts.txt");
        if (!Files.exists(teacherFile)) {
            return new ArrayList<>();
        }
        
        List<String> teachers = new ArrayList<>();
        List<String> lines = Files.readAllLines(teacherFile, StandardCharsets.UTF_8);
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            String[] parts = line.split(",", 2);
            if (parts.length >= 1 && !parts[0].isBlank()) {
                teachers.add(parts[0].trim());
            }
        }
        
        return teachers;
    }

    public static synchronized List<String> getStudentsWhoMessaged(String teacherName) throws IOException {
        ensureFile();
        Set<String> students = new LinkedHashSet<>();
        List<String> lines = Files.readAllLines(MESSAGE_FILE, StandardCharsets.UTF_8);
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 5) continue;
            
            String from = parts[0];
            String to = parts[1];
            boolean isFromStudent = Boolean.parseBoolean(parts[4]);
            
            // If message involves this teacher
            if (to.equalsIgnoreCase(teacherName) && isFromStudent) {
                students.add(from);
            } else if (from.equalsIgnoreCase(teacherName) && !isFromStudent) {
                students.add(to);
            }
        }
        
        return new ArrayList<>(students);
    }

    public static synchronized int getUnreadCount(String username, boolean isTeacher) throws IOException {
        ensureFile();
        int count = 0;
        List<String> lines = Files.readAllLines(MESSAGE_FILE, StandardCharsets.UTF_8);
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 6) continue;
            
            String to = parts[1];
            boolean isFromStudent = Boolean.parseBoolean(parts[4]);
            boolean isRead = "true".equalsIgnoreCase(parts[5]);
            
            // Unread messages sent TO this user
            if (to.equalsIgnoreCase(username) && !isRead) {
                // Teachers get notified of student messages, students get notified of teacher replies
                if (isTeacher && isFromStudent) {
                    count++;
                } else if (!isTeacher && !isFromStudent) {
                    count++;
                }
            }
        }
        
        return count;
    }

    public static synchronized void markAsRead(String reader, String sender) throws IOException {
        ensureFile();
        List<String> lines = Files.readAllLines(MESSAGE_FILE, StandardCharsets.UTF_8);
        StringBuilder updated = new StringBuilder();
        
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 5) {
                updated.append(line).append(System.lineSeparator());
                continue;
            }
            
            String from = parts[0];
            String to = parts[1];
            
            // Mark messages from sender to reader as read
            if (from.equalsIgnoreCase(sender) && to.equalsIgnoreCase(reader)) {
                updated.append(parts[0]).append("\t")
                       .append(parts[1]).append("\t")
                       .append(parts[2]).append("\t")
                       .append(parts[3]).append("\t")
                       .append(parts[4]).append("\t")
                       .append("true")
                       .append(System.lineSeparator());
            } else {
                if (parts.length >= 6) {
                    updated.append(line).append(System.lineSeparator());
                } else {
                    updated.append(line).append("\t").append("false").append(System.lineSeparator());
                }
            }
        }
        
        Files.writeString(MESSAGE_FILE, updated.toString(), StandardCharsets.UTF_8);
    }

    public static synchronized boolean hasUnreadFrom(String reader, String sender) throws IOException {
        ensureFile();
        List<String> lines = Files.readAllLines(MESSAGE_FILE, StandardCharsets.UTF_8);
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 6) continue;
            
            String from = parts[0];
            String to = parts[1];
            boolean isRead = "true".equalsIgnoreCase(parts[5]);
            
            if (from.equalsIgnoreCase(sender) && to.equalsIgnoreCase(reader) && !isRead) {
                return true;
            }
        }
        
        return false;
    }

    public static synchronized boolean editMessage(String from, String to, String oldTimestamp, String newMessage) 
            throws IOException {
        ensureFile();
        List<String> lines = Files.readAllLines(MESSAGE_FILE, StandardCharsets.UTF_8);
        StringBuilder updated = new StringBuilder();
        boolean edited = false;
        
        String sanitizedNewMessage = newMessage.replace("\t", " ").replace("\n", " ").trim();
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 5) {
                updated.append(line).append(System.lineSeparator());
                continue;
            }
            
            String msgFrom = parts[0];
            String msgTo = parts[1];
            String msgTimestamp = parts[3];
            
            // Match by sender, receiver, and timestamp
            if (msgFrom.equalsIgnoreCase(from) && msgTo.equalsIgnoreCase(to) && 
                msgTimestamp.equals(oldTimestamp)) {
                // Update the message
                updated.append(parts[0]).append("\t")
                       .append(parts[1]).append("\t")
                       .append(sanitizedNewMessage).append("\t")
                       .append(parts[3]).append("\t")
                       .append(parts[4]).append("\t")
                       .append(parts.length >= 6 ? parts[5] : "false")
                       .append(System.lineSeparator());
                edited = true;
            } else {
                updated.append(line).append(System.lineSeparator());
            }
        }
        
        if (edited) {
            Files.writeString(MESSAGE_FILE, updated.toString(), StandardCharsets.UTF_8);
        }
        
        return edited;
    }

    public static synchronized boolean deleteMessage(String from, String to, String timestamp) 
            throws IOException {
        ensureFile();
        List<String> lines = Files.readAllLines(MESSAGE_FILE, StandardCharsets.UTF_8);
        StringBuilder updated = new StringBuilder();
        boolean deleted = false;
        
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            
            String[] parts = line.split("\t", 6);
            if (parts.length < 5) {
                updated.append(line).append(System.lineSeparator());
                continue;
            }
            
            String msgFrom = parts[0];
            String msgTo = parts[1];
            String msgTimestamp = parts[3];
            
            // Match by sender, receiver, and timestamp to delete
            if (msgFrom.equalsIgnoreCase(from) && msgTo.equalsIgnoreCase(to) && 
                msgTimestamp.equals(timestamp)) {
                deleted = true;
                // Skip this line (don't add to updated)
            } else {
                updated.append(line).append(System.lineSeparator());
            }
        }
        
        if (deleted) {
            Files.writeString(MESSAGE_FILE, updated.toString(), StandardCharsets.UTF_8);
        }
        
        return deleted;
    }

    private static void ensureFile() throws IOException {
        Path parent = MESSAGE_FILE.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(MESSAGE_FILE)) {
            Files.createFile(MESSAGE_FILE);
        }
    }

    public record ChatMessage(String from, String to, String message, String timestamp, 
                              boolean isFromStudent, boolean isRead) {
    }
}
