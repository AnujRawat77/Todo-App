package com.todo;

import com.todo.model.Priority;
import com.todo.model.Todo;
import com.todo.persistence.Database;
import com.todo.persistence.PersistenceException;
import com.todo.persistence.SqliteTaskRepository;
import com.todo.persistence.TaskRepository;
import com.todo.service.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqliteTaskRepositoryIT {
    @TempDir
    Path tempDir;

    private String dbPath;
    private TaskRepository repo;

    @BeforeEach
    void setUp() {
        dbPath = tempDir.resolve("test.db").toString();
        repo = new SqliteTaskRepository(new Database(dbPath));
        repo.init();
    }

    @AfterEach
    void tearDown() {
        repo.close();
    }

    @Test
    void createAssignsIdsAndStoresPriority() {
        Todo a = repo.create("A", Priority.HIGH);
        Todo b = repo.create("B", Priority.LOW);
        assertTrue(b.getId() > a.getId());
        assertEquals(Priority.HIGH, repo.findById(a.getId()).get().getPriority());
        assertFalse(repo.findById(a.getId()).get().isCompleted());
    }

    @Test
    void findAllOrdersPendingFirstThenPriority() {
        repo.create("low", Priority.LOW);
        Todo high = repo.create("high", Priority.HIGH);
        repo.create("med", Priority.MEDIUM);
        repo.updateCompleted(high.getId(), true);
        List<Todo> all = repo.findAll();
        assertEquals("med", all.get(0).getTitle());
        assertEquals("low", all.get(1).getTitle());
        assertEquals("high", all.get(2).getTitle());
    }

    @Test
    void updateAndDelete() {
        Todo t = repo.create("A", Priority.MEDIUM);
        assertTrue(repo.updateCompleted(t.getId(), true));
        assertTrue(repo.findById(t.getId()).get().isCompleted());
        assertTrue(repo.deleteById(t.getId()));
        assertFalse(repo.findById(t.getId()).isPresent());
    }

    @Test
    void missingIdsReturnFalseAndChangeNothing() {
        repo.create("A", Priority.MEDIUM);
        assertFalse(repo.updateCompleted(999, true));
        assertFalse(repo.deleteById(999));
        assertEquals(1, repo.findAll().size());
        assertFalse(repo.findAll().get(0).isCompleted());
    }

    @Test
    void dataPersistsAcrossRepositoryInstances() {
        Todo a = repo.create("keep", Priority.HIGH);
        Todo b = repo.create("done", Priority.LOW);
        Todo c = repo.create("gone", Priority.MEDIUM);
        repo.updateCompleted(b.getId(), true);
        repo.deleteById(c.getId());
        repo.close();

        repo = new SqliteTaskRepository(new Database(dbPath));
        repo.init();
        List<Todo> all = repo.findAll();
        assertEquals(2, all.size());
        assertEquals(a.getId(), all.get(0).getId());
        assertTrue(repo.findById(b.getId()).get().isCompleted());
        assertFalse(repo.findById(c.getId()).isPresent());
    }

    @Test
    void idsAreNotReusedAfterDelete() {
        Todo a = repo.create("A", Priority.MEDIUM);
        repo.deleteById(a.getId());
        Todo b = repo.create("B", Priority.MEDIUM);
        assertTrue(b.getId() > a.getId());
    }

    @Test
    void serviceFiltersPersistedTasksAndValidatesIds() {
        TaskService service = new TaskService(repo);
        service.addTask("  trimmed  ", null);
        Todo high = service.addTask("urgent", Priority.HIGH);
        assertEquals(Priority.MEDIUM, service.listByPriority(Priority.MEDIUM).get(0).getPriority());
        assertEquals("trimmed", service.listByPriority(Priority.MEDIUM).get(0).getTitle());
        assertTrue(service.markCompleted(high.getId()));
        assertEquals(1, service.listPending().size());
        assertEquals(1, service.listCompleted().size());
        assertFalse(service.markCompleted(12345));
        assertFalse(service.deleteTask(12345));
        assertEquals(2, service.listAll().size());
        assertThrows(IllegalArgumentException.class, () -> service.addTask("   ", Priority.LOW));
    }

    // ---- failure paths (TC-33 / TC-49) ----

    @Test
    void openingDatabaseInMissingDirectoryThrowsPersistenceException() {
        String bad = tempDir.resolve("no-such-dir").resolve("x.db").toString();
        SqliteTaskRepository broken = new SqliteTaskRepository(new Database(bad));
        PersistenceException ex = assertThrows(PersistenceException.class, broken::init);
        assertTrue(ex.getMessage().contains(bad));
    }

    @Test
    void usingRepositoryBeforeInitThrowsPersistenceException() {
        SqliteTaskRepository uninit = new SqliteTaskRepository(new Database(dbPath));
        assertThrows(PersistenceException.class, uninit::findAll);
    }

    @Test
    void operationsAfterCloseThrowPersistenceException() {
        repo.close();
        assertThrows(PersistenceException.class, () -> repo.create("x", Priority.LOW));
        assertThrows(PersistenceException.class, () -> repo.deleteById(1));
        assertThrows(PersistenceException.class, () -> repo.updateCompleted(1, true));
    }
}
