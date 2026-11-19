package com.todo.service;

import com.todo.model.Priority;
import com.todo.model.Todo;
import com.todo.persistence.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskService {
    private final TaskRepository repo;

    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }

    public void initialize() {
        repo.init();
    }

    public void shutdown() {
        repo.close();
    }

    public Todo addTask(String title, Priority priorityOrNull) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        Priority p = priorityOrNull == null ? Priority.MEDIUM : priorityOrNull;
        return repo.create(title.trim(), p);
    }

    public List<Todo> listAll() {
        return filter(TaskFilter.all());
    }

    public List<Todo> listByPriority(Priority priority) {
        return filter(TaskFilter.byPriority(priority));
    }

    public List<Todo> listPending() {
        return filter(TaskFilter.pendingOnly());
    }

    public List<Todo> listCompleted() {
        return filter(TaskFilter.completedOnly());
    }

    public List<Todo> filter(TaskFilter filter) {
        return filter(repo.findAll(), filter);
    }

    /** Pure filtering helper (no persistence access). */
    public static List<Todo> filter(List<Todo> todos, TaskFilter filter) {
        List<Todo> result = new ArrayList<>();
        for (Todo todo : todos) {
            if (filter.matches(todo)) {
                result.add(todo);
            }
        }
        return result;
    }

    public Optional<Todo> findById(int id) {
        return repo.findById(id);
    }

    /** Returns false if the ID does not exist (no change made). */
    public boolean markCompleted(int id) {
        if (!repo.findById(id).isPresent()) {
            return false;
        }
        return repo.updateCompleted(id, true);
    }

    /** Returns false if the ID does not exist (no change made). */
    public boolean deleteTask(int id) {
        if (!repo.findById(id).isPresent()) {
            return false;
        }
        return repo.deleteById(id);
    }
}
