package com.todo.ui;

import com.todo.model.Priority;
import com.todo.model.Todo;
import com.todo.persistence.PersistenceException;
import com.todo.service.TaskService;
import com.todo.util.InputUtil;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

/** Console input/output only; all logic is delegated to TaskService. */
public class ConsoleApp {
    private static final String LINE = "----------------------------------------";

    private final TaskService service;
    private final Scanner scanner;

    public ConsoleApp(TaskService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void run() {
        boolean running = true;
        try {
            while (running) {
                showMainMenu();
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        handleAdd();
                        break;
                    case "2":
                        handleViewMenu();
                        break;
                    case "3":
                        handleMarkCompleted();
                        break;
                    case "4":
                        handleDelete();
                        break;
                    case "5":
                        running = false;
                        System.out.println("Goodbye.");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Input closed. Goodbye.");
        }
    }

    private void showMainMenu() {
        System.out.println();
        System.out.println(LINE);
        System.out.println("Todo Console App");
        System.out.println(LINE);
        System.out.println("1) Add Task");
        System.out.println("2) View Tasks");
        System.out.println("3) Mark Task Completed");
        System.out.println("4) Delete Task");
        System.out.println("5) Exit");
        System.out.println(LINE);
        System.out.print("Select an option: ");
    }

    void handleAdd() {
        printHeader("Add Task");
        String title = InputUtil.readNonEmptyString(scanner, "Title: ");
        Priority priority = InputUtil.readPriorityOrDefault(scanner,
                "Priority [HIGH|MEDIUM|LOW] (default: MEDIUM): ", Priority.MEDIUM);
        try {
            Todo created = service.addTask(title, priority);
            System.out.println("Task created: " + created);
        } catch (PersistenceException e) {
            reportError("ERROR: Could not add task. Please try again.", e);
        }
    }

    void handleViewMenu() {
        printHeader("View Tasks");
        System.out.println("1) View All");
        System.out.println("2) View Pending Only");
        System.out.println("3) View Completed Only");
        System.out.println("4) View By Priority");
        System.out.println("5) Back");
        System.out.println(LINE);
        String choice = InputUtil.readLine(scanner, "Select an option: ").trim();
        try {
            switch (choice) {
                case "1":
                    renderTodos(service.listAll());
                    break;
                case "2":
                    renderTodos(service.listPending());
                    break;
                case "3":
                    renderTodos(service.listCompleted());
                    break;
                case "4":
                    renderTodos(service.listByPriority(selectPriority()));
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } catch (PersistenceException e) {
            reportError("ERROR: Could not load tasks. Please try again.", e);
        }
    }

    private Priority selectPriority() {
        System.out.println("Select Priority:");
        System.out.println("1) HIGH");
        System.out.println("2) MEDIUM");
        System.out.println("3) LOW");
        while (true) {
            String sel = InputUtil.readLine(scanner, "Selection: ").trim();
            switch (sel) {
                case "1":
                    return Priority.HIGH;
                case "2":
                    return Priority.MEDIUM;
                case "3":
                    return Priority.LOW;
                default:
                    try {
                        Priority p = Priority.fromString(sel);
                        if (p != null) {
                            return p;
                        }
                    } catch (IllegalArgumentException ignored) {
                        // fall through and re-prompt
                    }
                    System.out.println("ERROR: Invalid selection. Enter 1, 2, 3, HIGH, MEDIUM or LOW.");
            }
        }
    }

    void renderTodos(List<Todo> todos) {
        System.out.println(LINE);
        System.out.println("Tasks");
        if (todos.isEmpty()) {
            System.out.println(LINE);
            System.out.println("No tasks matched your filter.");
        } else {
            System.out.println("Total shown: " + todos.size());
            System.out.println(LINE);
            for (Todo todo : todos) {
                System.out.println(todo);
            }
            System.out.println(LINE);
        }
        InputUtil.readLine(scanner, "(Press Enter to return)");
    }

    void handleMarkCompleted() {
        printHeader("Mark Task Completed");
        int id = InputUtil.readInt(scanner, "Enter Task ID: ");
        try {
            if (service.markCompleted(id)) {
                System.out.println("Task marked as completed.");
            } else {
                System.out.println("No task found with ID: " + id);
            }
        } catch (PersistenceException e) {
            reportError("ERROR: Could not update task. Please try again.", e);
        }
    }

    void handleDelete() {
        printHeader("Delete Task");
        int id = InputUtil.readInt(scanner, "Enter Task ID: ");
        try {
            Optional<Todo> todo = service.findById(id);
            if (!todo.isPresent()) {
                System.out.println("No task found with ID: " + id);
                return;
            }
            System.out.println("You are about to delete:");
            System.out.println(todo.get());
            String confirm = InputUtil.readLine(scanner, "Confirm delete (Y/N): ").trim();
            if (confirm.equalsIgnoreCase("Y")) {
                if (service.deleteTask(id)) {
                    System.out.println("Task deleted.");
                } else {
                    System.out.println("No task found with ID: " + id);
                }
            } else {
                System.out.println("Delete cancelled.");
            }
        } catch (PersistenceException e) {
            reportError("ERROR: Could not delete task. Please try again.", e);
        }
    }

    private void printHeader(String title) {
        System.out.println();
        System.out.println(LINE);
        System.out.println(title);
        System.out.println(LINE);
    }

    /** Friendly message on stdout; actionable detail (no stack trace) on stderr. */
    private void reportError(String userMessage, PersistenceException e) {
        System.out.println(userMessage);
        System.err.println("[DB error] " + e.getMessage());
    }
}
