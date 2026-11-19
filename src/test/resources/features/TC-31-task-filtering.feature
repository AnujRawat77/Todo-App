@TC-31
Feature: Filter tasks by priority and status

  Background:
    Given tasks: 1 HIGH pending, 2 HIGH done, 3 MEDIUM pending, 4 LOW done, 5 LOW pending

  Scenario: Filter by priority
    When the user filters by LOW
    Then tasks 4 and 5 are shown
    # Tests: TaskFilteringTest.byPriority

  Scenario: Filter pending or completed
    When the user lists pending tasks
    Then tasks 1, 3 and 5 are shown
    # Tests: TaskFilteringTest.pendingAndCompleted

  Scenario: Combined priority and status filter
    When the user filters by HIGH and completed
    Then only task 2 is shown
    # Tests: TaskFilteringTest.combinedPriorityAndStatus, TaskFilteringExtendedTest.*

  Scenario: No matches gives an empty list
    When the user filters by MEDIUM and completed
    Then no tasks are shown
    # Tests: TaskFilteringTest.noMatchesGivesEmptyList

  Scenario: Filters work on persisted data after restart
    Given tasks were saved and the app restarted
    When the user filters by HIGH
    Then the persisted HIGH tasks are shown
    # Tests: SqliteRepositoryExtendedIT.serviceFiltersWorkAfterReload
