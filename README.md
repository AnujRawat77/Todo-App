# Java Todo Application (Priority + SQLite)

A console-based Todo application written in Java. Tasks have a priority (HIGH / MEDIUM / LOW), can be filtered, and are saved to a local SQLite database so they survive restarts.

---

## Features

- Add tasks with priority (`HIGH`, `MEDIUM`, `LOW`)
  - priority input is case-insensitive
  - press Enter for the default `PRIORITY = MEDIUM`
  - invalid priority re-prompts
- View tasks with filters: All, Pending, Completed, by Priority
  - shows an empty-state message when nothing matches
- Mark a task as completed (by ID)
- Delete a task (with Y/N confirmation)
- SQLite persistence (auto-creates schema, persists after every action)
- Friendly error handling for missing IDs and DB failures

---

## Requirements

- JDK 11 or later
- Maven 3.6+

---

## Database location & configuration

By default, the SQLite database file is created in the current working directory as a relative path:

```text
./todo.db
```

The database path is resolved in this order of precedence (as implemented in `com.todo.Main.resolveDbPath`):

1. First command-line argument:
   ```bash
   java -jar target/todo-app-1.0.0.jar /path/to/my.db
   ```
2. JVM system property:
   ```bash
   java -Dtodo.db=/path/to/my.db -jar target/todo-app-1.0.0.jar
   ```
3. Environment variable:
  ```bash
  export TODO_DB_PATH=/path/to/my.db
  java -jar target/todo-app-1.0.0.jar
  ```
4. Default: `./todo.db`

Default schema:

```sql
CREATE TABLE IF NOT EXISTS tasks (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  completed INTEGER NOT NULL DEFAULT 0,
  priority TEXT NOT NULL DEFAULT 'MEDIUM',
  created_at TEXT NOT NULL DEFAULT (CURRENT_TIMESTAMP),
  CHECK (completed IN (0, 1)),
  CHECK (priority IN ('HIGH','MEDIUM','LOW'))
);
```

---

## How to build

Build with Maven:

```bash
mvn -q clean package
```

The executable fat JAR is produced at:

```text
target/todo-app-1.0.0.jar
```

> Note: there is no `build.sh`"€¾ `run.sh` on `main` at this time.

---

## How to run

Run the fat JAR:

```bash
java -jar target/todo-app-1.0.0.jar
```

Alternatively, run via Maven exec:

```bash
mvn -q compile exec:java -Dexec.mainClass=com.todo.Main
```

---

## How to test

```bash
mvn -q test
```

Notes:
 - Unit tests cover priority parsing/defaulting and filtering logic.
 - Integration tests (`SliteTaskRepositoryIT.java`) use a temporary SQLite file to verify CRUD, persistence across reload, and failure paths.


---

## Usage (new features)

### Priority

After you select `Add Task`, you'll be prompted for priority:

- Valid: `HIGH , `MEDIUM`, `LOW` (any case)
- Blank (just press Enter): defaults to `MEDIUM`

### Filtering

When you select `View Tasks`, you can choose:

- All tasks
- Pending only
- Completed only
- By Priority (prompts for HIGH/MEDIUM/LOW)

When no tasks match the selected filter, the app prints an empty-state message (e.g. `No tasks matched your filter.`).
