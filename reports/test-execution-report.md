# Test Execution Report (TC-30..TC-33)

Date: 2026-11-19  |  Branch: feature/priority-sqlite-enhancement  |  Command: mvn test (JUnit 5, Java 21)

**Total: 62  Passed: 62  Failed: 0  Errors: 0  Skipped: 0**

| Class | Tests | Passed | Failed | Errors | Skipped | Time (s) |
|---|---|---|---|---|---|---|
| com.todo.PriorityDefaultingTest | 17 | 17 | 0 | 0 | 0 | 0.428 |
| com.todo.PriorityParsingTest | 7 | 7 | 0 | 0 | 0 | 0.02 |
| com.todo.SqliteRepositoryExtendedIT | 17 | 17 | 0 | 0 | 0 | 0.265 |
| com.todo.SqliteTaskRepositoryIT | 10 | 10 | 0 | 0 | 0 | 0.143 |
| com.todo.TaskFilteringExtendedTest | 5 | 5 | 0 | 0 | 0 | 0.007 |
| com.todo.TaskFilteringTest | 6 | 6 | 0 | 0 | 0 | 0.002 |

Raw results: target/surefire-reports (XML/TXT).

Note: a resource leak in SqliteTaskRepository.init() (connection left open when schema init failed, locking corrupt DB file) was found by the new corrupt-DB test and fixed.
