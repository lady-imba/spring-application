package ru.bmstu.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import ru.bmstu.model.Student;
import ru.bmstu.model.User;
import ru.bmstu.service.LogService;
import ru.bmstu.service.StudentService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentServiceImpl implements StudentService {
    private final String csvFilePath;
    private final LogService logService;
    private List<Student> students;

    public StudentServiceImpl(String csvFilePath, LogService logService) {
        if (csvFilePath == null || csvFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV file path cannot be null or empty");
        }
        if (logService == null) {
            throw new IllegalArgumentException("LogService cannot be null");
        }
        
        System.out.println("Initializing StudentServiceImpl with CSV file path: " + csvFilePath);
        this.csvFilePath = csvFilePath;
        this.logService = logService;
        
        try {
            this.students = loadStudents();
        } catch (Exception e) {
            System.err.println("Error initializing StudentServiceImpl: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize StudentServiceImpl", e);
        }
    }

    private Path getCsvFilePath() {
        return Paths.get(csvFilePath).toAbsolutePath();
    }

    private void ensureFileExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write("firstName,lastName,tokens");
                writer.newLine();
            }
        }
    }

    private BufferedReader openReader(Path path) throws IOException {
        return Files.newBufferedReader(path);
    }

    private BufferedWriter openWriter(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        return Files.newBufferedWriter(path);
    }

    private boolean isHeaderValid(String header) {
        return header != null && header.equals("firstName,lastName,tokens");
    }

    private Student parseStudentLine(String line) {
        String[] values = line.split(",");
        if (values.length >= 3) {
            try {
                return new Student(
                    values[0].trim(),
                    values[1].trim(),
                    Integer.parseInt(values[2].trim())
                );
            } catch (NumberFormatException e) {
                System.err.println("Error parsing student tokens: " + e.getMessage());
            }
        }
        return null;
    }

    private List<Student> readStudentsFromFile(BufferedReader reader) throws IOException {
        List<Student> loadedStudents = new ArrayList<>();
        String header = reader.readLine();
        if (!isHeaderValid(header)) {
            return loadedStudents;
        }
        String line;
        while ((line = reader.readLine()) != null) {
            Student student = parseStudentLine(line);
            if (student != null) {
                loadedStudents.add(student);
            }
        }
        return loadedStudents;
    }

    private List<Student> loadStudents() {
        if (csvFilePath == null) {
            throw new IllegalStateException("CSV file path is null in loadStudents()");
        }
        Path path = getCsvFilePath();
        System.out.println("Loading students from file: " + path);
        try {
            ensureFileExists(path);
            try (BufferedReader reader = openReader(path)) {
                return readStudentsFromFile(reader);
            }
        } catch (IOException e) {
            System.err.println("Error loading students file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load students", e);
        }
    }

    private void saveStudents() {
        if (csvFilePath == null) {
            throw new IllegalStateException("CSV file path is null in saveStudents()");
        }
        Path path = getCsvFilePath();
        System.out.println("Saving students to file: " + path);
        try (BufferedWriter writer = openWriter(path)) {
            writer.write("firstName,lastName,tokens");
            writer.newLine();
            for (Student student : students) {
                writer.write(String.format("%s,%s,%d",
                    student.getFirstName(),
                    student.getLastName(),
                    student.getTokens()));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving students file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save students", e);
        }
    }

    private Optional<Student> findStudentByName(String firstName, String lastName) {
        return students.stream()
            .filter(s -> s.getFirstName().equals(firstName) && s.getLastName().equals(lastName))
            .findFirst();
    }

    private void ensureStudentExists(String firstName, String lastName) {
        if (findStudentByName(firstName, lastName).isEmpty()) {
            throw new IllegalArgumentException("Student not found: " + firstName + " " + lastName);
        }
    }

    private void ensureStudentNotExists(String firstName, String lastName) {
        if (findStudentByName(firstName, lastName).isPresent()) {
            throw new IllegalArgumentException("Student already exists: " + firstName + " " + lastName);
        }
    }

    @Override
    public void addStudent(User user, Student student) {
        ensureStudentNotExists(student.getFirstName(), student.getLastName());
        students.add(student);
        saveStudents();
        logService.logAction("ADD_STUDENT", user, 
            String.format("Added student: %s %s with %d tokens", 
                student.getFirstName(), student.getLastName(), student.getTokens()));
    }

    @Override
    public void expelStudent(User user, String firstName, String lastName) {
        ensureStudentExists(firstName, lastName);
        students.removeIf(s -> s.getFirstName().equals(firstName) && s.getLastName().equals(lastName));
        saveStudents();
        logService.logAction("EXPEL_STUDENT", user, 
            String.format("Expelled student: %s %s", firstName, lastName));
    }

    @Override
    public void updateTokens(String firstName, String lastName, int amount, User user) {
        ensureStudentExists(firstName, lastName);
        Student student = findStudentByName(firstName, lastName).get();
        int oldTokens = student.getTokens();
        student.setTokens(oldTokens + amount);
        saveStudents();
        logService.logAction("UPDATE_TOKENS", user, 
            String.format("Updated tokens for %s %s: %d -> %d", 
                firstName, lastName, oldTokens, student.getTokens()));
    }

    @Override
    public List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }

    @Override
    public void removeStudent(String firstName, String lastName, User user) {
        ensureStudentExists(firstName, lastName);
        students.removeIf(s -> s.getFirstName().equals(firstName) && s.getLastName().equals(lastName));
        saveStudents();
        logService.logAction("REMOVE_STUDENT", user, 
            String.format("Removed student: %s %s", firstName, lastName));
    }

    @Override
    public Student findStudent(String firstName, String lastName) {
        return findStudentByName(firstName, lastName).orElse(null);
    }
} 