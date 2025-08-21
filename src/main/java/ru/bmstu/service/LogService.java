package ru.bmstu.service;

import ru.bmstu.model.LogEntry;
import ru.bmstu.model.User;
import java.util.List;

public interface LogService {
    void logAction(String action, User user, String details);
    List<LogEntry> getAllLogs();
} 