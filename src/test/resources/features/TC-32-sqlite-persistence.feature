@TC-32
Feature: SQLite persistence

  Scenario: Create assigns database IDs and stores priority
    When the user adds "A" with HIGH priority
    Then the task is stored with a generated ID and priority HIGH
    # Tests: SqliteTaskRepositoryIT.createAssignsIdsAndStoresPriority

  Scenario: Update and delete
    Given an existing task
    When it is marked completed and then deleted
    Then it is no longer found
    # Tests: SqliteTaskRepositoryIT.updateAndDelete, SqliteRepositoryExtendedIT.updateCompletedCanToggleBackToPending

  Scenario: Tasks survive an application restart
    Given tasks with different priorities and statuses are saved
    When the repository is closed and reopened on the same database file
    Then all tasks, priorities and statuses are unchanged and deleted tasks stay deleted
    # Tests: SqliteTaskRepositoryIT.dataPersistsAcrossRepositoryInstances, SqliteRepositoryExtendedIT.allPrioritiesAndStatusesPersistAcrossReload

  Scenario: Special characters are stored safely
    When a title containing quotes, SQL and unicode is saved
    Then it is read back unchanged and the table is intact
    # Tests: SqliteRepositoryExtendedIT.specialCharactersAndUnicodeTitlesRoundTripSafely

  Scenario: Database enforces valid values
    When a row with priority URGENT, completed=5 or NULL title is inserted
    Then the database rejects it
    # Tests: SqliteRepositoryExtendedIT.schemaRejectsInvalidPriorityAndStatusValues
