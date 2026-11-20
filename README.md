# Java Todo Application (Priority + SQLite)

A console-based Todo application written in Java. Tasks have a priority
(HIGH / MEDIUM / LOW), can be filtered, and are saved to a local SQLite
database so they survive restarts.

## Quick start
Console-based Java Todo app with task priorities (LOW/MEDIUM/HIGH, case-insensitive, default MEDIUM), filtering by priority and status, and SQLite persistence (`./todo.db`).

```bash
./build.sh   # compile, run tests, package target/todo-app-1.0.0.jar
./run.sh     # run the app (builds first if the JAR is missing)
```

Tests use JUnit 5 (62 tests, run with `mvn test` or as part of `./build.sh`).

## Features
- Add a task with a priority (HIGH, MEDIUM, LOW; case-insensitive; press Enter for the default MEDIUM; invalid input re-prompts)
- View tasks: All, Pending only, Completed only, or By priority (with an empty-state message when nothing matches)
- Mark a task as completed
- Delete a task (with Y/N confirmation)
- Automatic persistence to SQLite after every action
- Graceful error handling: clear message and clean exit if the database cannot be opened, friendly messages for failed DB operations and for IDs that do not exist

## Requirements
- JDK 11 or later
- Maven 3.6+ (dependencies: `sqlite-jdbc`, JUnit 5 for tests)

## Database location
By default the database file is `./todo.db` in the directory you run the app from.
It is created automatically (schema included) on first start. Override it with, in order of precedence:

1. First command-line argument: `java -jar target/todo-app-1.0.0.jar /path/to/my.db`
2. JVM property: `-Dtodo.db=/path/to/my.db`
3. Environment variable: `TODO_DB_PATH=/path/to/my.db`

Schema:

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

Database errors are reported on the console in friendly form; technical details (no stack traces) are written to stderr.

## Project structure

```text
Todo-App/
├── pom.xml
├── src/
│   ├── main/java/com/todo/
│   │   ├── Main.java
│   │   ├── model/        Todo, Priority
│   │   ├── persistence/  Database, TaskRepository, SqliteTaskRepository, PersistenceException
│   │   ├── service/      TaskService, TaskFilter
│   │   ├── ui/           ConsoleApp
│   │   └── util/         InputUtil
│   └── test/java/com/todo/
│       ├── PriorityParsingTest.java
│       ├── TaskFilteringTest.java
│       └── SqliteTaskRepositoryIT.java
└── README.md
```

## How to build and run

```bash
mvn package
java -jar target/todo-app-1.0.0.jar
```

Or run directly from compiled classes with Maven:

```bash
mvn compile exec:java -Dexec.mainClass=com.todo.Main
```

## How to run the tests

```bash
mvn test
```

Unit tests cover priority parsing/defaulting and filtering; integration tests
(`SqliteTaskRepositoryIT`) use a temporary SQLite file to verify CRUD, reload
persistence and failure paths.
