package ru.bmstu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.bmstu.model.Student;
import ru.bmstu.model.User;
import ru.bmstu.service.impl.LogServiceImpl;
import ru.bmstu.service.impl.StudentServiceImpl;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StudentServiceTest {
    private StudentService studentService;
    private User teacher;
    private User student;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Path testLogs = tempDir.resolve("test-logs.csv");
        Path testStudents = tempDir.resolve("test-students.csv");

        LogService logService = new LogServiceImpl(testLogs.toString());
        studentService = new StudentServiceImpl(testStudents.toString(), logService);
        teacher = new User("John", "Doe", User.UserRole.TEACHER);
        student = new User("Jane", "Smith", User.UserRole.STUDENT);
    }

    @Test
    void testAddStudent() {
        Student newStudent = new Student("Alice", "Johnson", 0);
        studentService.addStudent(teacher, newStudent);

        Student found = studentService.findStudent("Alice", "Johnson");
        assertNotNull(found);
        assertEquals("Alice", found.getFirstName());
        assertEquals(0, found.getTokens());
    }

    @Test
    void testUpdateTokens() {
        Student newStudent = new Student("Bob", "Wilson", 10);
        studentService.addStudent(teacher, newStudent);

        studentService.updateTokens("Bob", "Wilson", 5, teacher);
        Student updated = studentService.findStudent("Bob", "Wilson");
        assertEquals(15, updated.getTokens());
    }

    @Test
    void testRemoveStudent() {
        Student newStudent = new Student("Charlie", "Brown", 0);
        studentService.addStudent(teacher, newStudent);

        studentService.removeStudent("Charlie", "Brown", teacher);
        Student removed = studentService.findStudent("Charlie", "Brown");
        assertNull(removed);
    }

    @Test
    void testGetAllStudents() {
        studentService.addStudent(teacher, new Student("Alice", "Johnson", 5));
        studentService.addStudent(teacher, new Student("Bob", "Wilson", 10));

        assertEquals(2, studentService.getAllStudents().size());
    }
}