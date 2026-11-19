package com.todo;

import com.todo.model.Priority;
import com.todo.model.Todo;
import com.todo.persistence.Database;
import com.todo.persistence.SqliteTaskRepository;
import com.todo.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Extra coverage for TC-30: priority parsing and defaulting. */
class PriorityDefaultingTest {
    @TempDir
    Path tempDir;

    @ParameterizedTest
    @CsvSource({"HiGh,HIGH", "mEdIuM,MEDIUM", "lOw,LOW", "H,HIGH", "m,MEDIUM", "' l ',LOW"})
    void mixedCaseAndWhitespaceParse(String raw, Priority expected) {
        assertEquals(expected, Priority.fromString(raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"urgent", "1", "hh", "high priority", "x", "critical"})
    void invalidValuesThrowWithOffendingValueInMessage(String raw) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Priority.fromString(raw));
        assertTrue(ex.getMessage().contains(raw));
    }

    @Test
    void toStringIsUpperCaseName() {
        assertEquals("HIGH", Priority.HIGH.toString());
        assertEquals("LOW", Priority.LOW.toString());
    }

    @Test
    void todoTwoArgConstructorDefaultsToPendingMedium() {
        Todo t = new Todo(1, "x");
        assertEquals(Priority.MEDIUM, t.getPriority());
        assertFalse(t.isCompleted());
    }

    @Test
    void setPriorityNullFallsBackToMedium() {
        Todo t = new Todo(1, "x", false, Priority.HIGH);
        t.setPriority(null);
        assertEquals(Priority.MEDIUM, t.getPriority());
        t.setPriority(Priority.LOW);
        assertEquals(Priority.LOW, t.getPriority());
    }

    @Test
    void toStringShowsIdStatusPriorityAndTitle() {
        Todo t = new Todo(3, "Buy milk", false, Priority.HIGH);
        assertEquals("[ID: 3] [PENDING] [HIGH] Buy milk", t.toString());
        t.markCompleted();
        assertEquals("[ID: 3] [COMPLETED] [HIGH] Buy milk", t.toString());
    }

    @Test
    void serviceDefaultsToMediumAndPersistsIt() {
        String db = tempDir.resolve("d.db").toString();
        TaskService svc = new TaskService(new SqliteTaskRepository(new Database(db)));
        svc.initialize();
        try {
            assertEquals(Priority.MEDIUM, svc.addTask("no priority given", null).getPriority());
        } finally {
            svc.shutdown();
        }
        SqliteTaskRepository again = new SqliteTaskRepository(new Database(db));
        again.init();
        try {
            assertEquals(Priority.MEDIUM, again.findAll().get(0).getPriority());
        } finally {
            again.close();
        }
    }
}
