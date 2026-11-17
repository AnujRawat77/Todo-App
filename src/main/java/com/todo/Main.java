package com.todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final List<Todo> todos = new ArrayList<>();
    private static int nextId = 1;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("===== Simple Todo Application =====");

        while (running) {
            System.out.println("\n1. Add Todo");
            System.out.println("2. View Todos");
            System.out.println("3. Mark Todo as Completed");
            System.out.println("4. Delete Todo");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addTodo(scanner);
                    break;
                case "2":
                    viewTodos();
                    break;
                case "3":
                    markTodoCompleted(scanner);
                    break;
                case "4":
                    deleteTodo(scanner);
                    break;
                case "5":
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
    }

    private static void addTodo(Scanner scanner) {
        System.out.print("Enter todo title: ");
        String title = scanner.nextLine().trim();

        if (title.isEmpty()) {
            System.out.println("Todo title cannot be empty.");
            return;
        }

        todos.add(new Todo(nextId++, title));
        System.out.println("Todo added successfully.");
    }

    private static void viewTodos() {
        if (todos.isEmpty()) {
            System.out.println("No todos found.");
            return;
        }

        System.out.println("\n----- Todo List -----");
        for (Todo todo : todos) {
            String status = todo.isCompleted() ? "Completed" : "Pending";
            System.out.println(todo.getId() + ". " + todo.getTitle() + " [" + status + "]");
        }
    }

    private static void markTodoCompleted(Scanner scanner) {
        System.out.print("Enter the todo ID to mark as completed: ");
        int id = readId(scanner);

        if (id == -1) {
            return;
        }

        Todo todo = findTodoById(id);
        if (todo == null) {
            System.out.println("Todo not found.");
        } else {
            todo.markCompleted();
            System.out.println("Todo marked as completed.");
        }
    }

    private static void deleteTodo(Scanner scanner) {
        System.out.print("Enter the todo ID to delete: ");
        int id = readId(scanner);

        if (id == -1) {
            return;
        }

        Todo todo = findTodoById(id);
        if (todo == null) {
            System.out.println("Todo not found.");
        } else {
            todos.remove(todo);
            System.out.println("Todo deleted successfully.");
        }
    }

    private static int readId(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid numeric ID.");
            return -1;
        }
    }

    private static Todo findTodoById(int id) {
        for (Todo todo : todos) {
            if (todo.getId() == id) {
                return todo;
            }
        }
        return null;
    }
}
