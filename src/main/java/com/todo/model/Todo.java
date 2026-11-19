package com.todo.model;

public class Todo {
    private final int id;
    private final String title;
    private boolean completed;
    private Priority priority;

    public Todo(int id, String title) {
        this(id, title, false, Priority.MEDIUM);
    }

    public Todo(int id, String title, boolean completed, Priority priority) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.priority = priority == null ? Priority.MEDIUM : priority;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void markCompleted() {
        completed = true;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority == null ? Priority.MEDIUM : priority;
    }

    @Override
    public String toString() {
        return "[ID: " + id + "] [" + (completed ? "COMPLETED" : "PENDING") + "] ["
                + priority + "] " + title;
    }
}
