# Code Review Findings (TC-29)

Branch: `feature/priority-sqlite-enhancement`
Scope: Priority + filtering + SQLite persistence (Jira TC-29..TC-33)


## Overall Assessment
Approve. Implementation appears correct for Priority, filtering, and SQLite persistence, with solid JDBC hygiene (PreparedStatements, try-with-resources) and meaningful unit/integration tests.

Main improvement areas: connection lifecycle robustness (single long-lived Connection), close() exception behavior, and minor hardening for unexpected DB values.
B

## Notable Positives (Praise)
- Clear separation of concerns: ConsoleUI „ TaskService ₞ TaskRepository, with SQLite persistence isolated in `SqliteTaskRepository`/``Database``.
- Good JDBC hygiene overall: parameterized queries, try-with-resources, and schema guards with `CREATE TABLE IF NOT EXISTS`.
 - Indexes are added to support common queries.
 - CHECK constraints/defaults are a nice touch (e.g. `completed` and `priority`).
- Integration tests validate persistence across repository instances, ordering, and negative/error paths.

- Priority parsing is robust (trim, case-insensitive, aliases H/M/L, null/blank results in null so service or DB picks default).


## Comments / Improvements

### [Major] SQLite connection lifecycle robustness
- The repository appears to hold a single long-lived `Connection` per run. This is fine for a single-user console app, but not resilient if the connection is dropped mid-run.
  - Consider opening connections per operation, or adding a validity check/reconnect strategy, or at least documenting the limitation.

### [Major] close() exception behavior
- close() throws on close failures. In shutdown paths this can be noisy and mask original failures.
  - Prefer log-and-suppress (or a `quietly` close) endless callers need to handle close failures.

### [Minor] Hardening for unexpected DB values
- Row mapping uses `Priority.valueOf` on the priority string. If DB contains unexpected values (manual edits, old schema, corruption), this will throw an unwrapped `IllegalArgumentException`.
  - Consider catching and wrapping to `PersistenceException` with clear diagnostics, or mapping unknown values to `MEDIUM`.
 
### [Major] Test api usage pattern
- In the integration test, the service is constructed with repo but `TaskService.initialize()` is not called in at least one test; the test passes only because repo.init() was called in setUp().
  - Recommend initializing through the service in tests to encode the correct usage pattern and avoid future misuse.

### [Nit] Test phrasing / lifecycle
- If desired later, consider using Failsafe for `*IT.java` to keep unit and integration tests in separate maven phases. Current setup okay for this project size.


## Traceability to Jira TC-30..TC-33 (implemented behavior)
- TC-30 Priority end-to-end: model + input parsing + persistence + display.
- TC-31 Filtering: all/pending/completed + by-priority, with combination support via filter object.
 - TC-32 SQLite persistence: integration tests confirm state survives across new repository instances.
- TC-33 Robustness: Error handling present via domain exceptions, and negative path tests cover invalid paths, using repo before init, and operations after close.
