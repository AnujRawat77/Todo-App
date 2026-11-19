package com.todo;

import com.todo.model.Priority;
import com.todo.model.Todo;
import com.todo.service.TaskFilter;
import com.todo.service.TaskService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskFilteringTest {
    private final List<Todo> todos = Arrays.asList(
            new Todo(1, "a", false, Priority.HIGH),
            new Todo(2, "b", true, Priority.HIGH),
            new Todo(3, "c", false, Priority.MEDIUM),
            new Todo(4, "d", true, Priority.LOW),
            new Todo(5, "e", false, Priority.LOW));

    private List<Integer> ids(TaskFilter f) {
        List<Integer> ids = new java.util.ArrayList<>();
        for (Todo t : TaskService.filter(todos, f)) {
            ids.add(t.getId());
        }
        return ids;
    }

    @Test
    void allReturnsEverything() {
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), ids(TaskFilter.all()));
    }

    @Test
    void byPriority() {
        assertEquals(Arrays.asList(1, 2), ids(TaskFilter.byPriority(Priority.HIGH)));
        assertEquals(Arrays.asList(3), ids(TaskFilter.byPriority(Priority.MEDIUM)));
        assertEquals(Arrays.asList(4, 5), ids(TaskFilter.byPriority(Priority.LOW)));
    }

    @Test
    void pendingAndCompleted() {
        assertEquals(Arrays.asList(1, 3, 5), ids(TaskFilter.pendingOnly()));
        assertEquals(Arrays.asList(2, 4), ids(TaskFilter.completedOnly()));
    }

    @Test
    void combinedPriorityAndStatus() {
        assertEquals(Arrays.asList(5), ids(TaskFilter.of(Priority.LOW, false)));
        assertEquals(Arrays.asList(2), ids(TaskFilter.of(Priority.HIGH, true)));
    }

    @Test
    void noMatchesGivesEmptyList() {
        assertTrue(TaskService.filter(todos, TaskFilter.of(Priority.MEDIUM, true)).isEmpty());
        assertTrue(TaskService.filter(Collections.<Todo>emptyList(), TaskFilter.all()).isEmpty());
    }

    @Test
    void todoDefaultsToMediumWhenPriorityNull() {
        assertEquals(Priority.MEDIUM, new Todo(9, "x", false, null).getPriority());
    }
}
