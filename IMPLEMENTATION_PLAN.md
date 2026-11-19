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
```

- Use `AUTOINCREMENT` for stable IDs; return generated keys on insert.

### Filtering
- Implement in the service layer.
- For simplicity: list all, then filter in Java (acceptable for a console app).

### Input Validation
- Parse priority case-insensitively.
- Default to `MEDIUM` on empty input.
- Re-prompt until valid.

### Error Handling
- Repository throws a custom `PersistenceException`.
- UI catches and prints user-friendly messages.
- Exit cleanly on startup DB-open failure.

### Testing
- **JUnit 5** unit tests: priority parsing + filtering logic.
- **Integration tests**: `SqliteTaskRepository` with a temp DB file (CRUD + reload).
- Deterministic tests: create schema per test, clean tables.

---

## 3. Affected Files

| File | Change |
|------|--------|
| `IMPLEMENTATION_PLAN.md` | New |
| `README.md` | Update (priority, filtering, DB location) |
| `src/main/java/**/Main.java` | Refactor to ConsoleApp/Menu + service usage |
| `src/main/java/**/model/Todo.java` | Add priority field |
| `src/main/java/**/model/Priority.java` | New enum (HIGH/MEDIUM/LOW) |
| `src/main/java/**/persistence/Database.java` | New — connection + schema init |
| `src/main/java/**/persistence/TaskRepository.java` | New interface |
| `src/main/java/**/persistence/SqliteTaskRepository.java` | New JDBC implementation |
| `src/main/java/**/service/TaskService.java` | New — business logic + filtering |
| `src/main/java/**/service/TaskFilter.java` | Optional — filter criteria |
| `src/main/java/**/util/InputUtil.java` | Optional — safe input parsing |
| `pom.xml` / `build.gradle` | Add sqlite-jdbc, junit-jupiter |
| `src/test/java/**/PriorityParsingTest.java` | New |
| `src/test/java/**/TaskFilteringTest.java` | New |
| `src/test/java/**/SqliteTaskRepositoryIT.java` | New |

---

## 4. Implementation Steps

| # | Task | Story | Effort |
|---|------|-------|--------|
| 1 | Create IMPLEMENTATION_PLAN.md (scope, file map, sequencing, estimates, testing strategy) | — | 1.5h |
| 2 | Codebase review + lightweight refactor plan; decide repository-first vs in-memory sync; define target structure | — | 1.0h |
| 3 | Implement `Priority` enum; extend `Todo` with priority (default MEDIUM); update list formatting | TC-30 | 2.0h |
| 4 | Update Add-Todo flow: prompt priority, parse case-insensitively, default MEDIUM, re-prompt on invalid; unit tests | TC-30, TC-37 | 2.5h |
| 5 | Implement filtering UX: menu options (All / Pending / Completed / By Priority), empty-state messaging, filter logic + unit tests | TC-31, TC-41 | 4.0h |
| 6 | Add SQLite dependency + DB bootstrap; `Database` helper runs CREATE TABLE IF NOT EXISTS; decide DB file location (`./todo.db`) | TC-32 | 2.5h |
| 7 | Implement `TaskRepository` + `SqliteTaskRepository` (JDBC CRUD): create, list, updateCompleted, delete; prepared statements; type mapping | TC-32 | 5.0h |
| 8 | Refactor app flow to use repository; init DB on startup; remove in-memory counter; IDs come from DB | TC-32 | 4.0h |
| 9 | Graceful error handling: `PersistenceException`; handle DB-open failure; wrap CRUD failures; validate ID existence | TC-33 | 3.0h |
| 10 | Integration tests: temp DB, verify CRUD + persistence across new repository instance; negative-path tests | TC-32, TC-33 | 4.0h |
| 11 | Polish + docs: update README, formatting cleanup, consistent messaging, manual flow verification | — | 1.5h |

**Total Estimated Effort:** ~31 hours

---

## 5. Dependencies

- Jira items already created: Epic **TC-29**; Stories **TC-30..TC-33** and tasks **TC-34..TC-49** (traceability).
- Build tool (`pom.xml` / `build.gradle`) to add `sqlite-jdbc` and `junit-jupiter`.
- Java version compatibility with chosen sqlite-jdbc version (JDK 8+).
- Ability to write a DB file in the runtime working directory.

---

## 6. Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Existing code may be a single class; refactoring risks regressions | Incremental refactor backed by unit/integration tests |
| SQLite locking / connection lifecycle issues | Single connection per run OR try-with-resources per operation; document approach |
| ID handling mismatch (gaps after deletes) | Ensure UI tolerates non-contiguous IDs; clear messaging |
| Ambiguous priority inputs (H/M/L) | Decide alias support and test |
| Test flakiness with filesystem SQLite DB | Use temp directories; delete DB files after tests; avoid order dependence |

---

## 7. Testing Approach

### Unit Tests (JUnit 5)
- Priority parsing/defaulting — case-insensitivity + invalid retry behavior.
- Filtering logic — priority/status using deterministic in-memory lists.

### Integration Tests
- `SqliteTaskRepository` against a temp SQLite DB file.
- Create schema → run CRUD → recreate repository → validate reload persistence.
- Validate update/delete for missing IDs (no changes, returns false/0 rows).

### Manual Smoke Test
- Start app → add tasks with each priority → filter by priority and pending → mark completed → exit → restart → confirm persistence and stable IDs.

---

## 8. Definition of Done

- [ ] Priority feature implemented and displayed (TC-30)
- [ ] Filtering by priority/status works (TC-31)
- [ ] SQLite persistence functional with repository layer (TC-32)
- [ ] Robust error handling in place (TC-33)
- [ ] Unit + integration tests passing
- [ ] README updated
- [ ] Code committed to `feature/priority-sqlite-enhancement`
- [ ] Pull Request opened against `main`
