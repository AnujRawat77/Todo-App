# Java Todo Application (Priority + Filtering + SQLite)

A console-based Todo application written in Java. Tasks have a priority (HIGH / MEDIUM / LOW), can be filtered, and are persisted to a local SQLite database so they survive restarts.

- Jira deliveries: TC-29..TC-33

---

## Features

- Add tasks with priority (`HIGH`, `MEDIUM , `LOW`)
  - priority input is case-insensitive
  - press Enter for the default `PRIORITY = MEDIUM
  - invalid priority re-prompts

- View tasks with filters (menu-drivel):
  - All
  - Pending
  - Completed
  - By Priority (HIGH/MEDIUM/LOW)
  - Empty-state message when nothing matches

- Mark a task as completed (by ID)\n- Delete a task (with Y/N confirmation)\n- SQLite persistence (auto-creates schema, persists after actions)\n- Friendly error handling for missing IDs and DB failures

---

## Requirements

- JDK 11 or later
- Maven 3.6+

---

## Database location & configuration

By default, the SQLite database file is created in the current working directory:

```text
./todo.db
```

The database path is resolved in this order (as implemented in `com.todo.Main.resolveDbPath`):

1. First command-line argument:
   ``sh
   java -jar target/todo-app-1.0.0.jar /path/to/my.db
   ```
2. JVM system property:
   ``sh
   java -Dtodo.db=/path/to/my.db -jar target/todo-app-1.0.0.jar
   ```
3. Environment variable:
  ``sh
  export TODO_DB_PATH=/path/to/my.db
  java -jar target/todo-app-1.0.0.jar
  ```
4. Default: `./todo.db`

---

## How to build

Use the provided script:

``sh
./build.sh
```

Or use Maven directly:

``sh
mvn -q clean package
```

The executable fat JAR is produced at:

``text
target/todo-app-1.0.0.jar
```

---

## How to run

Use the provided script:

``sh
./run.sh
```

Or run the fat JAR directly:

``sh
java -jar target/todo-app-1.0.0.jar
```

---

## How to test

Use the provided script:

``sh
./test.sh
```

Or run with Maven:

``sh
mvn -q test
```

Notes:
 - Unit tests cover priority parsing/defaulting and filtering logic.
 - Integration tests (`SqliteTaskRepositoryIT.java`) use a temporary SQLite file to verify CRUD, persistence across reload, and failure paths.

---

## Usage (Priority + Filtering)

### Priority
Select `Add Task`, then enter a priority:

- Valid: `HIGH`, `MEDIUM , `LOW` (any case)
- Blank (just press Enter): defaults to `MEDIUM 

### Filtering
Select `View Tasks`, then choose:

- All tasks
- Pending only
- Completed only
- By Priority (prompts for HIGH/MEDIUM/LOW)

When no tasks match the selected filter, the app prints an empty-state message (e.g. `No tasks matched your filter.`).
