# ISA Layer (L2): Implementation/API Level
# Source: swagger.yaml - /auth/login endpoint
# Maps DSL scenarios to concrete HTTP requests

@isa
Feature: User Login API Implementation

  Background:
    # Clean slate for each scenario - tests should create their own data

  Scenario: Successful login with valid credentials
    # Setup: Register a test user first
    Given I send "POST" request to "/auth/register" with body:
      """
      {
        "username": "Alice",
        "password": "Test1234!"
      }
      """
    Then the response status code should be 201

    # Action: Login with valid credentials
    When I send "POST" request to "/auth/login" with body:
      """
      {
        "username": "Alice",
        "password": "Test1234!"
      }
      """

    # Verification: Check response matches Swagger spec
    Then the response status code should be 200
    And the response body should contain field "accessToken"
    And the response body field "user.username" should equal "Alice"
    And the response body field "user.experience" should equal "0"

  Scenario: Failed login with wrong password
    # Setup: Register a test user first
    Given I send "POST" request to "/auth/register" with body:
      """
      {
        "username": "Bob",
        "password": "Correct123!"
      }
      """
    Then the response status code should be 201

    # Action: Login with wrong password
    When I send "POST" request to "/auth/login" with body:
      """
      {
        "username": "Bob",
        "password": "WrongPassword!"
      }
      """

    # Verification: Check error response matches Swagger spec
    Then the response status code should be 401
    And the response body field "error" should equal "帳號或密碼錯誤"

  Scenario: Failed login with non-existent username
    # Action: Login with non-existent user
    When I send "POST" request to "/auth/login" with body:
      """
      {
        "username": "NonExistentUser",
        "password": "AnyPassword123!"
      }
      """

    # Verification: Check error response matches Swagger spec
    Then the response status code should be 401
    And the response body field "error" should equal "帳號或密碼錯誤"
