package com.todo.service;

import com.todo.model.Priority;
import com.todo.model.Todo;

public class TaskFilter {
    private final Priority priority;
    private final Boolean completed;

    private TaskFilter(Priority priority, Boolean completed) {
        this.priority = priority;
        this.completed = completed;
    }

    public static TaskFilter all() {
        return new TaskFilter(null, null);
    }

    public static TaskFilter byPriority(Priority priority) {
        return new TaskFilter(priority, null);
    }

    public static TaskFilter pendingOnly() {
        return new TaskFilter(null, Boolean.FALSE);
    }

    public static TaskFilter completedOnly() {
        return new TaskFilter(null, Boolean.TRUE);
    }

    public static TaskFilter of(Priority priority, Boolean completed) {
        return new TaskFilter(priority, completed);
    }

    public Priority getPriority() {
        return priority;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public boolean matches(Todo todo) {
        return (priority == null || todo.getPriority() == priority)
                && (completed == null || todo.isCompleted() == completed);
    }
}
