package ru.bmstu.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEntry {
    private LocalDateTime timestamp;
    private String action;
    private String userFirstName;
    private String userLastName;
    private User.UserRole userRole;
    private String details;
} 