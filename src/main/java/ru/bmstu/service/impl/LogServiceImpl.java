package ru.bmstu.service.impl;

import org.springframework.beans.factory.annotation.Value;
import ru.bmstu.model.LogEntry;
import ru.bmstu.model.User;
import ru.bmstu.service.LogService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogServiceImpl implements LogService {
    private final List<LogEntry> logs;
    private final String logFilePath;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    //trim() — удаляет все ведущие и завершающие пробелы
    public LogServiceImpl(String logFilePath) {
        if (logFilePath == null || logFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Log file path cannot be null or empty");
        }

        System.out.println("Initializing LogServiceImpl with log file path: " + logFilePath);
        this.logFilePath = logFilePath;
        try {
            this.logs = loadLogs();
        } catch (Exception e) {
            System.err.println("Error initializing LogServiceImpl: " + e.getMessage());
            e.printStackTrace(); //Отображает стек вызовов
            throw new RuntimeException("Failed to initialize LogServiceImpl", e);
        }
    }

    private List<LogEntry> loadLogs() {
        List<LogEntry> loadedLogs = new ArrayList<>();
        
        if (logFilePath == null) {
            throw new IllegalStateException("Log file path is null in loadLogs()");
        }
        
        System.out.println("Loading logs from file: " + logFilePath);
        Path path = Paths.get(logFilePath).toAbsolutePath();
        System.out.println("Absolute path: " + path);
        
        try {
            if (!Files.exists(path)) {
                System.out.println("Creating log file at: " + path);
                Files.createDirectories(path.getParent());
                //записывает данные напрямую в файл без использования буферов
                try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                    writer.write("timestamp,action,userFirstName,userLastName,userRole,details");
                    writer.newLine();
                }
            }
            
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String header = reader.readLine();
                if (header == null) {
                    return loadedLogs;
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",", 6);
                    if (values.length >= 6) {
                        try {
                            loadedLogs.add(new LogEntry(
                                LocalDateTime.parse(values[0].trim(), DATE_FORMATTER),
                                values[1].trim(),
                                values[2].trim(),
                                values[3].trim(),
                                User.UserRole.valueOf(values[4].trim()),
                                values[5].trim()
                            ));
                        } catch (Exception e) {
                            System.err.println("Error parsing log entry: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading log file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load logs", e);
        }
        return loadedLogs;
    }

    private void saveLogs() {
        if (logFilePath == null) {
            throw new IllegalStateException("Log file path is null in saveLogs()");
        }
        
        System.out.println("Saving logs to file: " + logFilePath);
        Path path = Paths.get(logFilePath).toAbsolutePath();
        System.out.println("Absolute path: " + path);
        
        try {
            Files.createDirectories(path.getParent());// Создание директорий (если их нет)
            
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write("timestamp,action,userFirstName,userLastName,userRole,details");
                writer.newLine();
                
                for (LogEntry log : logs) {
                    writer.write(String.format("%s,%s,%s,%s,%s,%s",
                        log.getTimestamp().format(DATE_FORMATTER),
                        log.getAction(),
                        log.getUserFirstName(),
                        log.getUserLastName(),
                        log.getUserRole(),
                        log.getDetails()));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving log file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save logs", e);
        }
    }

    @Override
    public void logAction(String action, User user, String details) {
        if (action == null || user == null) {
            throw new IllegalArgumentException("Action and user cannot be null");
        }
        
        LogEntry logEntry = new LogEntry(
            LocalDateTime.now(),
            action,
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            details != null ? details : ""
        );
        logs.add(logEntry);
        saveLogs();
    }

    @Override
    public List<LogEntry> getAllLogs() {
        return new ArrayList<>(logs);
    }
}

