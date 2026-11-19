package com.todo;

import com.todo.persistence.Database;
import com.todo.persistence.PersistenceException;
import com.todo.persistence.SqliteTaskRepository;
import com.todo.service.TaskService;
import com.todo.ui.ConsoleApp;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String dbPath = resolveDbPath(args);
        TaskService service = new TaskService(new SqliteTaskRepository(new Database(dbPath)));

        System.out.println("Todo Console App (Persisted)");
        System.out.println("DB: " + dbPath);

        try {
            service.initialize();
        } catch (PersistenceException e) {
            System.out.println("ERROR: Could not open database at " + dbPath);
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Application will exit.");
            System.exit(1);
            return;
        }

        Scanner scanner = new Scanner(System.in);
        try {
            new ConsoleApp(service, scanner).run();
        } finally {
            scanner.close();
            try {
                service.shutdown();
            } catch (PersistenceException e) {
                System.err.println("[DB error] " + e.getMessage());
            }
        }
    }

    /** Order: first CLI argument, then -Dtodo.db, then TODO_DB_PATH env var, then ./todo.db. */
    static String resolveDbPath(String[] args) {
        if (args != null && args.length > 0 && !args[0].trim().isEmpty()) {
            return args[0].trim();
        }
        String prop = System.getProperty("todo.db");
        if (prop != null && !prop.trim().isEmpty()) {
            return prop.trim();
        }
        String env = System.getenv("TODO_DB_PATH");
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        return Database.DEFAULT_DB_PATH;
    }
}
