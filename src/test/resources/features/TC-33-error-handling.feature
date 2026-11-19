@TC-33
Feature: Robust error handling

  Scenario Outline: Invalid task ID
    When the user tries to complete or delete task <id>
    Then the operation reports "not found" and nothing changes
    Examples:
      | id  |
      | 999 |
      | 0   |
      | -1  |
    # Tests: SqliteTaskRepositoryIT.missingIdsReturnFalseAndChangeNothing, SqliteRepositoryExtendedIT.invalidIdsAreNotFound, serviceHandlesInvalidIdsGracefully

  Scenario: Non-numeric ID input re-prompts
    When the user enters "abc" then "7" as an ID
    Then 7 is accepted
    # Tests: PriorityParsingTest.readIntRepromptsUntilNumeric

  Scenario: Empty title is rejected
    When the user adds a blank or null title
    Then an error "Title cannot be empty." is raised and nothing is saved
    # Tests: SqliteTaskRepositoryIT.serviceFiltersPersistedTasksAndValidatesIds, SqliteRepositoryExtendedIT.serviceRejectsNullTitle

  Scenario: Database cannot be opened
    Given the database path is in a missing directory or the file is corrupt
    When the app initialises
    Then a PersistenceException naming the problem is raised and no file handle leaks
    # Tests: SqliteTaskRepositoryIT.openingDatabaseInMissingDirectory..., SqliteRepositoryExtendedIT.corruptDatabaseFileThrowsPersistenceException, persistenceExceptionKeepsCause

  Scenario: Repository used before init or after close
    Then PersistenceException is thrown instead of a raw SQL/NPE error
    # Tests: SqliteTaskRepositoryIT.usingRepositoryBeforeInit..., operationsAfterClose..., SqliteRepositoryExtendedIT.serviceSurfacesRepositoryFailureAsPersistenceException
