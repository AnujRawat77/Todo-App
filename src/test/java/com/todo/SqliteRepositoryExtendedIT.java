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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/** Extra coverage for TC-32/TC-33: CRUD, persistence on reload, error handling. */
class SqliteRepositoryExtendedIT {
    @TempDir
    Path tempDir;

    private String dbPath;
    private TaskRepository repo;

    @BeforeEach
    void setUp() {
        dbPath = tempDir.resolve("ext.db").toString();
        repo = new SqliteTaskRepository(new Database(dbPath));
        repo.init();
    }

    @AfterEach
    void tearDown() {
        repo.close();
    }

    private void reopen() {
        repo.close();
        repo = new SqliteTaskRepository(new Database(dbPath));
        repo.init();
    }

    @Test
    void emptyDatabaseReturnsEmptyList() {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void invalidIdsAreNotFound() {
        assertFalse(repo.findById(0).isPresent());
        assertFalse(repo.findById(-5).isPresent());
        assertFalse(repo.findById(Integer.MAX_VALUE).isPresent());
        assertFalse(repo.deleteById(-1));
        assertFalse(repo.updateCompleted(0, true));
    }

    @Test
    void serviceHandlesInvalidIdsGracefully() {
        TaskService svc = new TaskService(repo);
        assertFalse(svc.markCompleted(-1));
        assertFalse(svc.deleteTask(0));
        assertFalse(svc.findById(42).isPresent());
    }

    @Test
    void serviceRejectsNullTitle() {
        TaskService svc = new TaskService(repo);
        assertThrows(IllegalArgumentException.class, () -> svc.addTask(null, Priority.HIGH));
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void deletingTwiceReturnsFalseSecondTime() {
        Todo t = repo.create("once", Priority.LOW);
        assertTrue(repo.deleteById(t.getId()));
        assertFalse(repo.deleteById(t.getId()));
    }

    @Test
    void updateCompletedCanToggleBackToPending() {
        Todo t = repo.create("toggle", Priority.MEDIUM);
        repo.updateCompleted(t.getId(), true);
        repo.updateCompleted(t.getId(), false);
        assertFalse(repo.findById(t.getId()).get().isCompleted());
    }

    @Test
    void specialCharactersAndUnicodeTitlesRoundTripSafely() {
        String evil = "x\"); DROP TABLE tasks;-- é中";
        Todo t = repo.create(evil, Priority.HIGH);
        reopen();
        assertEquals(evil, repo.findById(t.getId()).get().getTitle());
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void allPrioritiesAndStatusesPersistAcrossReload() {
        Todo h = repo.create("h", Priority.HIGH);
        Todo m = repo.create("m", Priority.MEDIUM);
        Todo l = repo.create("l", Priority.LOW);
        repo.updateCompleted(l.getId(), true);
        reopen();
        assertEquals(Priority.HIGH, repo.findById(h.getId()).get().getPriority());
        assertEquals(Priority.MEDIUM, repo.findById(m.getId()).get().getPriority());
        assertEquals(Priority.LOW, repo.findById(l.getId()).get().getPriority());
        assertTrue(repo.findById(l.getId()).get().isCompleted());
        assertFalse(repo.findById(h.getId()).get().isCompleted());
    }

    @Test
    void serviceFiltersWorkAfterReload() {
        TaskService svc = new TaskService(repo);
        svc.addTask("a", Priority.HIGH);
        Todo b = svc.addTask("b", Priority.HIGH);
        svc.addTask("c", Priority.LOW);
        svc.markCompleted(b.getId());
        reopen();
        TaskService svc2 = new TaskService(repo);
        assertEquals(2, svc2.listByPriority(Priority.HIGH).size());
        assertEquals(1, svc2.listByPriority(Priority.LOW).size());
        assertEquals(2, svc2.listPending().size());
        assertEquals(1, svc2.listCompleted().size());
    }

    @Test
    void initIsIdempotentOnExistingDatabase() {
        repo.create("keep", Priority.LOW);
        reopen();
        reopen();
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void closeIsSafeToCallTwice() {
        repo.close();
        assertDoesNotThrow(repo::close);
    }

    @Test
    void schemaRejectsInvalidPriorityAndStatusValues() throws SQLException {
        Database db = new Database(dbPath);
        try (Connection c = db.openConnection(); Statement st = c.createStatement()) {
            assertThrows(SQLException.class,
                    () -> st.execute("INSERT INTO tasks (title, priority) VALUES ('x','URGENT')"));
            assertThrows(SQLException.class,
                    () -> st.execute("INSERT INTO tasks (title, completed) VALUES ('x', 5)"));
            assertThrows(SQLException.class,
                    () -> st.execute("INSERT INTO tasks (title) VALUES (NULL)"));
        }
    }

    @Test
    void rawInsertWithoutPriorityDefaultsToMediumInDatabase() throws SQLException {
        Database db = new Database(dbPath);
        try (Connection c = db.openConnection(); Statement st = c.createStatement()) {
            st.execute("INSERT INTO tasks (title) VALUES ('raw')");
        }
        reopen();
        Todo t = repo.findAll().get(0);
        assertEquals(Priority.MEDIUM, t.getPriority());
        assertFalse(t.isCompleted());
    }

    @Test
    void corruptDatabaseFileThrowsPersistenceException() throws Exception {
        Path bad = tempDir.resolve("corrupt.db");
        Files.write(bad, "this is definitely not a sqlite database file, just garbage text"
                .getBytes(StandardCharsets.UTF_8));
        SqliteTaskRepository broken = new SqliteTaskRepository(new Database(bad.toString()));
        assertThrows(PersistenceException.class, broken::init);
    }

    @Test
    void persistenceExceptionKeepsCause() {
        String bad = tempDir.resolve("missing").resolve("x.db").toString();
        PersistenceException ex = assertThrows(PersistenceException.class,
                () -> new Database(bad).openConnection());
        assertNotNull(ex.getCause());
    }

    @Test
    void serviceSurfacesRepositoryFailureAsPersistenceException() {
        TaskService svc = new TaskService(new SqliteTaskRepository(new Database(dbPath))); // never initialized
        assertThrows(PersistenceException.class, () -> svc.addTask("x", Priority.LOW));
        assertThrows(PersistenceException.class, svc::listAll);
        assertThrows(PersistenceException.class, () -> svc.markCompleted(1));
        assertThrows(PersistenceException.class, () -> svc.deleteTask(1));
    }

    @Test
    void databaseExposesPathAndUrl() {
        Database db = new Database("./x.db");
        assertEquals("./x.db", db.getDbFilePath());
        assertEquals("jdbc:sqlite:./x.db", db.getDbUrl());
    }
}
