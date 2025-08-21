package ru.bmstu.service;

import ru.bmstu.model.Student;
import ru.bmstu.model.User;
import java.util.List;

public interface StudentService {
    List<Student> getAllStudents();
    void addStudent(User user, Student student);
    void removeStudent(String firstName, String lastName, User user);
    void expelStudent(User user, String firstName, String lastName);
    void updateTokens(String firstName, String lastName, int amount, User user);
    Student findStudent(String firstName, String lastName);
} 