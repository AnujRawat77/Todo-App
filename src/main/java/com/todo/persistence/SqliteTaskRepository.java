package com.todo.persistence;

import com.todo.model.Priority;
import com.todo.model.Todo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation using a single long-lived connection per run.
 * The connection is validated before each operation and transparently reopened
 * if it has been dropped. After {@link #close()} the repository must be re-initialized.
 */
public class SqliteTaskRepository implements TaskRepository {
    private static final String SELECT_COLUMNS = "SELECT id, title, completed, priority FROM tasks";

    private final Database db;
    private Connection connection;

    public SqliteTaskRepository(Database db) {
        this.db = db;
    }

    @Override
    public void init() {
        connection = db.openConnection();
        db.ensureSchema(connection);
    }

    private Connection conn() {
        if (connection == null) {
            throw new PersistenceException("Repository is not initialized.");
        }
        try {
            if (!connection.isValid(2)) {
                quietlyClose(connection);
                connection = db.openConnection();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Could not validate database connection: " + e.getMessage(), e);
        }
        return connection;
    }

    private static void quietlyClose(Connection c) {
        try {
            c.close();
        } catch (SQLException e) {
            System.err.println("Warning: could not close database connection: " + e.getMessage());
        }
    }

    @Override
    public Todo create(String title, Priority priority) {
        String sql = "INSERT INTO tasks (title, completed, priority) VALUES (?, 0, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, priority.name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Todo(keys.getInt(1), title, false, priority);
                }
            }
            throw new PersistenceException("Could not add task: no ID was generated.");
        } catch (SQLException e) {
            throw new PersistenceException("Could not add task: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Todo> findAll() {
        String sql = SELECT_COLUMNS + " ORDER BY completed ASC, "
                + "CASE priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END ASC, id ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Todo> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Could not load tasks: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Todo> findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement(SELECT_COLUMNS + " WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Could not load task " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateCompleted(int id, boolean completed) {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE tasks SET completed = ? WHERE id = ?")) {
            ps.setInt(1, completed ? 1 : 0);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new PersistenceException("Could not update task " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM tasks WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new PersistenceException("Could not delete task " + id + ": " + e.getMessage(), e);
        }
    }

    /** Closes the connection; failures are logged and suppressed so shutdown never throws. */
    @Override
    public void close() {
        if (connection != null) {
            Connection c = connection;
            connection = null;
            quietlyClose(c);
        }
    }

    private Todo mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String raw = rs.getString("priority");
        Priority priority;
        try {
            priority = Priority.valueOf(raw == null ? "" : raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: task " + id + " has unknown priority '" + raw
                    + "'; treating as MEDIUM.");
            priority = Priority.MEDIUM;
        }
        return new Todo(id, rs.getString("title"), rs.getInt("completed") == 1, priority);
    }
}
