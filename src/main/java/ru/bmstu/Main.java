package ru.bmstu;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.bmstu.config.AppConfig;
import ru.bmstu.model.Student;
import ru.bmstu.model.User;
import ru.bmstu.service.StudentService;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        StudentService studentService = context.getBean(StudentService.class);

        System.out.println("Welcome to Student Management System");
        User currentUser = login(scanner);

        while (true) {
            System.out.println("\nAvailable actions:");
            System.out.println("1. View all students");
            System.out.println("2. Update tokens");
            System.out.println("3. Add new student");
            System.out.println("4. Remove student");
            System.out.println("5. Exit");

            int choice = getValidIntInput(scanner, "Choose action (1-5): ", 1, 5);

            try {
                switch (choice) {
                    case 1:
                        viewAllStudents(studentService);
                        break;
                    case 2:
                        updateTokens(studentService, currentUser, scanner);
                        break;
                    case 3:
                        addNewStudent(studentService, currentUser, scanner);
                        break;
                    case 4:
                        removeStudent(studentService, currentUser, scanner);
                        break;
                    case 5:
                        System.out.println("Goodbye!");
                        context.close();
                        scanner.close();
                        return;
                }
            } catch (SecurityException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }
    }

    private static User login(Scanner scanner) {
        String firstName = getValidNameInput(scanner, "Enter your first name: ");
        String lastName = getValidNameInput(scanner, "Enter your last name: ");

        User.UserRole role;
        while (true) {
            String roleStr = getNonEmptyInput(scanner, "Enter your role (TEACHER/STUDENT): ").toUpperCase();
            if (roleStr.equals("TEACHER") || roleStr.equals("STUDENT")) {
                role = User.UserRole.valueOf(roleStr);
                break;
            }
            System.out.println("Invalid role. Please enter either TEACHER or STUDENT.");
        }

        return new User(firstName, lastName, role);
    }

    private static void viewAllStudents(StudentService studentService) {
        System.out.println("\nCurrent students:");
        studentService.getAllStudents().forEach(student ->
                System.out.printf("%s %s: %d tokens%n",
                        student.getFirstName(),
                        student.getLastName(),
                        student.getTokens())
        );
    }

    private static void updateTokens(StudentService studentService, User currentUser, Scanner scanner) {
        String firstName = getValidNameInput(scanner, "Enter student's first name: ");
        String lastName = getValidNameInput(scanner, "Enter student's last name: ");
        int amount = getValidIntInput(scanner, "Enter token amount to add/subtract: ");

        try {
            studentService.updateTokens(firstName, lastName, amount, currentUser);
            System.out.println("Tokens updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void addNewStudent(StudentService studentService, User currentUser, Scanner scanner) {
        String firstName = getValidNameInput(scanner, "Enter new student's first name: ");
        String lastName = getValidNameInput(scanner, "Enter new student's last name: ");

        try {
            Student newStudent = new Student(firstName, lastName, 0);
            studentService.addStudent(currentUser, newStudent);
            System.out.println("Student added successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void removeStudent(StudentService studentService, User currentUser, Scanner scanner) {
        String firstName = getValidNameInput(scanner, "Enter student's first name to remove: ");
        String lastName = getValidNameInput(scanner, "Enter student's last name to remove: ");

        try {
            studentService.removeStudent(firstName, lastName, currentUser);
            System.out.println("Student removed successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static boolean isValidName(String name) {
        return name.matches("[a-zA-Zа-яА-Я-]+");
    }

    private static String getValidNameInput(Scanner scanner, String prompt) {
        String input;
        do {
            input = getNonEmptyInput(scanner, prompt);
            if (!isValidName(input)) {
                System.out.println("Invalid input. Only letters and hyphens are allowed.");
            }
        } while (!isValidName(input));
        return input;
    }

    private static String getNonEmptyInput(Scanner scanner, String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
            }
        } while (input.isEmpty());
        return input;
    }

    private static int getValidIntInput(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int input = scanner.nextInt();
                scanner.nextLine(); // очистка буфера
                if (input >= min && input <= max) {
                    return input;
                } else {
                    System.out.printf("Input must be between %d and %d. Please try again.%n", min, max);
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // очистка некорректного ввода
            }
        }
    }

    private static int getValidIntInput(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int input = scanner.nextInt();
                scanner.nextLine(); // очистка буфера
                return input;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // очистка некорректного ввода
            }
        }
    }
}