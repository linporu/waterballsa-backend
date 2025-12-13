# ISA Layer (L2): Implementation/API Level
# Source: swagger.yaml - /auth/register endpoint
# Maps DSL scenarios to concrete HTTP requests

@isa
Feature: User Registration API Implementation

  Background:
    # Database is cleaned before each scenario (handled by @Before hook)

  Scenario: Successful registration with valid credentials
    # No setup needed - user doesn't exist yet

    # Action: Register new user with valid credentials
    When I send "POST" request to "/auth/register" with body:
      """
      {
        "username": "Alice",
        "password": "Test1234!"
      }
      """

    # Verification: HTTP layer
    Then the response status code should be 201

    # Verification: Response structure
    And the response body should contain field "message"
    And the response body should contain field "userId"

    # Verification: Response values
    And the response body field "message" should equal "Registration successful"

  Scenario: Attempt to register with existing username
    # Setup: Create test user in database
    Given the database has a user:
      | username | Bob         |
      | password | Secure123!  |

    # Action: Attempt to register with same username
    When I send "POST" request to "/auth/register" with body:
      """
      {
        "username": "Bob",
        "password": "NewPassword456!"
      }
      """

    # Verification: HTTP layer
    Then the response status code should be 409

    # Verification: Error response
    And the response body should contain field "error"
    And the response body field "error" should equal "使用者名稱已存在"

  Scenario: Register with minimum username length (3 characters)
    # No setup needed - user doesn't exist yet

    # Action: Register with 3-character username
    When I send "POST" request to "/auth/register" with body:
      """
      {
        "username": "Tom",
        "password": "Valid123!"
      }
      """

    # Verification: HTTP layer
    Then the response status code should be 201

    # Verification: Response structure
    And the response body should contain field "message"
    And the response body should contain field "userId"

    # Verification: Response values
    And the response body field "message" should equal "Registration successful"

  Scenario: Register with maximum username length (50 characters)
    # No setup needed - user doesn't exist yet

    # Action: Register with 50-character username
    When I send "POST" request to "/auth/register" with body:
      """
      {
        "username": "alice_chen_waterball_student_learning_java_202512",
        "password": "Strong123!"
      }
      """

    # Verification: HTTP layer
    Then the response status code should be 201

    # Verification: Response structure
    And the response body should contain field "message"
    And the response body should contain field "userId"

    # Verification: Response values
    And the response body field "message" should equal "Registration successful"

  Scenario: Register with minimum password length (8 characters)
    # No setup needed - user doesn't exist yet

    # Action: Register with 8-character password
    When I send "POST" request to "/auth/register" with body:
      """
      {
        "username": "Charlie",
        "password": "Pass123!"
      }
      """

    # Verification: HTTP layer
    Then the response status code should be 201

    # Verification: Response structure
    And the response body should contain field "message"
    And the response body should contain field "userId"

    # Verification: Response values
    And the response body field "message" should equal "Registration successful"

  Scenario: Register with maximum password length (72 characters)
    # No setup needed - user doesn't exist yet

    # Action: Register with 72-character password
    When I send "POST" request to "/auth/register" with body:
      """
      {
        "username": "Diana",
        "password": "A1!bcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@$%*"
      }
      """

    # Verification: HTTP layer
    Then the response status code should be 201

    # Verification: Response structure
    And the response body should contain field "message"
    And the response body should contain field "userId"

    # Verification: Response values
    And the response body field "message" should equal "Registration successful"

  Scenario: Failed registration with username too short (2 characters)
    # No setup needed - testing validation

    # Action: Attempt to register with 2-character username
    When I send "POST" request to "/auth/register" with body:
      """
      {
        "username": "Ed",
        "password": "Valid123!"
      }
      """

    # Verification: HTTP layer
    Then the response status code should be 400

    # Verification: Error response
    And the response body should contain field "error"
    And the response body field "error" should equal "資料驗證失敗，請檢查輸入內容"

  Scenario: Failed registration with username too long (51 characters)
    # No setup needed - testing validation

    # Action: Attempt to register with 51-character username
    When I send "POST" request to "/auth/register" with body:
      """
      {
        "username": "frank_johnson_waterball_student_learning_python_2025",
        "password": "Valid123!"
      }
      """

    # Verification: HTTP layer
    Then the response status code should be 400

    # Verification: Error response
    And the response body should contain field "error"
    And the response body field "error" should equal "資料驗證失敗，請檢查輸入內容"

  Scenario: Failed registration with invalid username characters
    # No setup needed - testing validation

    # Action: Attempt to register with special characters in username
    When I send "POST" request to "/auth/register" with body:
      """
      {
        "username": "alice@chen",
        "password": "Valid123!"
      }
      """

    # Verification: HTTP layer
    Then the response status code should be 400

    # Verification: Error response
    And the response body should contain field "error"
    And the response body field "error" should equal "資料驗證失敗，請檢查輸入內容"

  Scenario: Failed registration with password too short (7 characters)
    # No setup needed - testing validation

    # Action: Attempt to register with 7-character password
    When I send "POST" request to "/auth/register" with body:
      """
      {
        "username": "Grace",
        "password": "Pass12!"
      }
      """

    # Verification: HTTP layer
    Then the response status code should be 400

    # Verification: Error response
    And the response body should contain field "error"
    And the response body field "error" should equal "資料驗證失敗，請檢查輸入內容"

  Scenario: Failed registration with password too long (73 characters)
    # No setup needed - testing validation

    # Action: Attempt to register with 73-character password
    When I send "POST" request to "/auth/register" with body:
      """
      {
        "username": "Henry",
        "password": "A1!bcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@$%*?&#A"
      }
      """

    # Verification: HTTP layer
    Then the response status code should be 400

    # Verification: Error response
    And the response body should contain field "error"
    And the response body field "error" should equal "資料驗證失敗，請檢查輸入內容"
