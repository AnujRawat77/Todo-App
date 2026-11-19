package com.todo.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens SQLite connections and initializes the schema. */
public class Database {
    public static final String DEFAULT_DB_PATH = "./todo.db";

    private final String dbFilePath;
    private final String dbUrl;

    public Database(String dbFilePath) {
        this.dbFilePath = dbFilePath;
        this.dbUrl = "jdbc:sqlite:" + dbFilePath;
    }

    public String getDbFilePath() {
        return dbFilePath;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public Connection openConnection() {
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            return conn;
        } catch (SQLException e) {
            throw new PersistenceException("Could not open database at " + dbFilePath
                    + ": " + e.getMessage(), e);
        }
    }

    public void ensureSchema(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS tasks ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "title TEXT NOT NULL, "
                    + "completed INTEGER NOT NULL DEFAULT 0, "
                    + "priority TEXT NOT NULL DEFAULT 'MEDIUM', "
                    + "created_at TEXT NOT NULL DEFAULT (CURRENT_TIMESTAMP), "
                    + "CHECK (completed IN (0, 1)), "
                    + "CHECK (priority IN ('HIGH','MEDIUM','LOW')))");
            st.execute("CREATE INDEX IF NOT EXISTS idx_tasks_completed ON tasks(completed)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority)");
        } catch (SQLException e) {
            throw new PersistenceException("Could not initialize database schema: "
                    + e.getMessage(), e);
        }
    }
}
