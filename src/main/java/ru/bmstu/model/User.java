package ru.bmstu.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String firstName;
    private String lastName;
    private UserRole role;


    public enum UserRole {
        STUDENT,
        TEACHER
    }
} 