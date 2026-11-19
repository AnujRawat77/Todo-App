package com.todo.persistence;

import com.todo.model.Priority;
import com.todo.model.Todo;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    void init();

    Todo create(String title, Priority priority);

    List<Todo> findAll();

    Optional<Todo> findById(int id);

    /** Returns true if exactly one row was updated, false if the id does not exist. */
    boolean updateCompleted(int id, boolean completed);

    /** Returns true if exactly one row was deleted, false if the id does not exist. */
    boolean deleteById(int id);

    void close();
}
