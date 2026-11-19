package com.todo;

import com.todo.model.Priority;
import com.todo.model.Todo;
import com.todo.service.TaskFilter;
import com.todo.service.TaskService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Extra coverage for TC-31: filtering logic. */
class TaskFilteringExtendedTest {
    private final List<Todo> todos = Arrays.asList(
            new Todo(1, "a", false, Priority.HIGH),
            new Todo(2, "b", true, Priority.MEDIUM),
            new Todo(3, "c", false, Priority.LOW));

    @Test
    void filterGettersExposeCriteria() {
        TaskFilter f = TaskFilter.of(Priority.HIGH, true);
        assertEquals(Priority.HIGH, f.getPriority());
        assertEquals(Boolean.TRUE, f.getCompleted());
        assertNull(TaskFilter.all().getPriority());
        assertNull(TaskFilter.all().getCompleted());
        assertEquals(Boolean.FALSE, TaskFilter.pendingOnly().getCompleted());
    }

    @Test
    void ofWithNullsBehavesLikeAll() {
        assertEquals(3, TaskService.filter(todos, TaskFilter.of(null, null)).size());
    }

    @Test
    void filterDoesNotMutateSourceAndPreservesOrder() {
        List<Todo> result = TaskService.filter(todos, TaskFilter.pendingOnly());
        assertEquals(3, todos.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(3, result.get(1).getId());
        assertNotSame(todos, TaskService.filter(todos, TaskFilter.all()));
    }

    @Test
    void matchesChecksEachCriterion() {
        Todo t = todos.get(0);
        assertTrue(TaskFilter.byPriority(Priority.HIGH).matches(t));
        assertFalse(TaskFilter.byPriority(Priority.LOW).matches(t));
        assertTrue(TaskFilter.pendingOnly().matches(t));
        assertFalse(TaskFilter.completedOnly().matches(t));
    }

    @Test
    void everyPriorityStatusCombinationPartitionsList() {
        int total = 0;
        for (Priority p : Priority.values()) {
            for (boolean done : new boolean[]{false, true}) {
                total += TaskService.filter(todos, TaskFilter.of(p, done)).size();
            }
        }
        assertEquals(todos.size(), total);
    }
}
