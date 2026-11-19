package com.todo.util;

import com.todo.model.Priority;

import java.util.Scanner;

/** Safe console input helpers. */
public final class InputUtil {
    private InputUtil() {
    }

    public static String readLine(Scanner sc, String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    /** Re-prompts until a non-empty value is entered. */
    public static String readNonEmptyString(Scanner sc, String prompt) {
        while (true) {
            String value = readLine(sc, prompt).trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("ERROR: Title cannot be empty.");
        }
    }

    /** Re-prompts until a valid integer is entered. */
    public static int readInt(Scanner sc, String prompt) {
        while (true) {
            String value = readLine(sc, prompt).trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Please enter a valid numeric value.");
            }
        }
    }

    /** Re-prompts until HIGH/MEDIUM/LOW is entered; blank returns the default. */
    public static Priority readPriorityOrDefault(Scanner sc, String prompt, Priority defaultValue) {
        while (true) {
            String raw = readLine(sc, prompt);
            try {
                Priority p = Priority.fromString(raw);
                return p == null ? defaultValue : p;
            } catch (IllegalArgumentException e) {
                System.out.println("ERROR: Invalid priority. Enter HIGH, MEDIUM, LOW, or press Enter for "
                        + defaultValue + ".");
            }
        }
    }
}
