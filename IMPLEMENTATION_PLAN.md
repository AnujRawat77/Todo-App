# Implementation Plan: Priority & SQLite Persistence Enhancement

**Project:** Todo-App (Java Console Application)
**Repository:** https://github.com/AnujRawat77/Todo-App
**Jira Epic:** TC-29
**Related Stories:** TC-30, TC-31, TC-32, TC-33
**Branch:** `feature/priority-sqlite-enhancement`
**Status:** Pending Approval

---

## 1. Overview

This plan covers the enhancement of the Java console Todo application to:

1. Add task **Priority** levels (HIGH / MEDIUM / LOW) with filtering (TC-30, TC-31)
2. Introduce **SQLite persistence** with a repository/DAO layer (TC-32)
3. Refactor the console flow to use persistence
4. Add robust **error handling** (TC-33)

---

## 2. Technical Approach

### Architecture
Refactor from a monolithic `Main` class into clean layers:

| Layer | Responsibility |
|-------|----------------|
| **Domain Model** | `Todo`, `Priority` |
| **Repository Layer** | `TaskRepository` interface + `SqliteTaskRepository` (JDBC) |
| **Service Layer** | `TaskService` — add/list/filter/complete/delete + validation |
| **Console UI Layer** | `ConsoleApp` / `Menu` — input/output only |

### Persistence
- Use **SQLite via JDBC** (`org.xerial:sqlite-jdbc`).
- Initialize schema on startup with:

```sql
CREATE TABLE IF NOT EXISTS tasks (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    title      TEXT NOT NULL,
    completed  INTEGER NOT NULL DEFAULT 0,
    priority   TEXT NOT NULL DEFAULT 'MEDIUM',
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);
