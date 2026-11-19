@TC-30
Feature: Task priority levels
  Acceptance criteria derived from IMPLEMENTATION_PLAN.md (Jira text was not retrievable).

  Scenario Outline: Priority is parsed case-insensitively
    When the user enters "<input>" as the priority
    Then the task priority is <priority>
    Examples:
      | input  | priority |
      | high   | HIGH     |
      | Medium | MEDIUM   |
      | LOW    | LOW      |
      | h      | HIGH     |
    # Tests: PriorityParsingTest.parsesCaseInsensitively, PriorityDefaultingTest.mixedCaseAndWhitespaceParse

  Scenario: Priority defaults to MEDIUM when left blank
    When the user presses Enter at the priority prompt
    Then the task priority is MEDIUM
    # Tests: PriorityParsingTest.readPriorityDefaultsToMediumOnEmptyInput, PriorityDefaultingTest.serviceDefaultsToMediumAndPersistsIt

  Scenario: Invalid priority re-prompts
    When the user enters "urgent" and then "low"
    Then an error is shown once and the task priority is LOW
    # Tests: PriorityParsingTest.readPriorityRepromptsUntilValid, PriorityDefaultingTest.invalidValuesThrow...

  Scenario: Priority is shown in the task listing
    Given a pending HIGH task "Buy milk" with ID 3
    Then it is displayed as "[ID: 3] [PENDING] [HIGH] Buy milk"
    # Tests: PriorityDefaultingTest.toStringShowsIdStatusPriorityAndTitle
