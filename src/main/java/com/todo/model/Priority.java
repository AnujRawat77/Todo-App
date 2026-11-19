package com.todo.model;

public enum Priority {
    HIGH, MEDIUM, LOW;

    /**
     * Parses a priority (case-insensitive, trimmed). Accepts H/M/L aliases.
     * Returns null for null/blank input so the caller can apply the default.
     *
     * @throws IllegalArgumentException if the value is not a valid priority
     */
    public static Priority fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String value = raw.trim().toUpperCase();
        switch (value) {
            case "HIGH":
            case "H":
                return HIGH;
            case "MEDIUM":
            case "M":
                return MEDIUM;
            case "LOW":
            case "L":
                return LOW;
            default:
                throw new IllegalArgumentException("Invalid priority: " + raw.trim());
        }
    }

    @Override
    public String toString() {
        return name();
    }
}
