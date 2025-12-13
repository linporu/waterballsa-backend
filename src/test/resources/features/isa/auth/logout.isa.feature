# ISA Layer (L2): Implementation/API Level
# Source: swagger.yaml - /auth/logout endpoint
# Maps DSL scenarios to concrete HTTP requests

@isa
Feature: User Logout API Implementation

  Background:
    # Database is cleaned before each scenario (handled by @Before hook)

  Scenario: Logged-in user successfully logs out
    # Setup: Create test user directly in database
    Given the database has a user:
      | username   | Alice     |
      | password   | Test1234! |
      | experience | 0         |

    # Action: Login to get access token
    When I send "POST" request to "/auth/login" with body:
      """
      {
        "username": "Alice",
        "password": "Test1234!"
      }
      """

    # Verification: Login successful
    Then the response status code should be 200
    And the response body should contain field "accessToken"

    # Store token for logout
    And I store the response field "accessToken" as "token"

    # Action: Logout with valid token
    When I send "POST" request to "/auth/logout" with authorization "{{token}}"

    # Verification: HTTP layer
    Then the response status code should be 200

    # Verification: Response structure
    And the response body should contain field "message"

    # Verification: Response values
    And the response body field "message" should equal "Logout successful"

  Scenario: Non-logged-in user attempts to logout and fails
    # No setup needed - no user logged in

    # Action: Attempt logout without Authorization header
    When I send "POST" request to "/auth/logout"

    # Verification: HTTP layer
    Then the response status code should be 401

    # Verification: Error response
    And the response body should contain field "error"
    And the response body field "error" should equal "登入資料已過期"

  Scenario: Already logged-out user attempts to logout again and fails
    # Setup: Create test user directly in database
    Given the database has a user:
      | username   | Bob         |
      | password   | Secure123!  |
      | experience | 0           |

    # Action: Login to get access token
    When I send "POST" request to "/auth/login" with body:
      """
      {
        "username": "Bob",
        "password": "Secure123!"
      }
      """

    # Verification: Login successful
    Then the response status code should be 200
    And the response body should contain field "accessToken"

    # Store token for first logout
    And I store the response field "accessToken" as "token"

    # Action: First logout (token will be blacklisted)
    When I send "POST" request to "/auth/logout" with authorization "{{token}}"

    # Verification: First logout successful
    Then the response status code should be 200
    And the response body field "message" should equal "Logout successful"

    # Action: Attempt second logout with same (now blacklisted) token
    When I send "POST" request to "/auth/logout" with authorization "{{token}}"

    # Verification: HTTP layer
    Then the response status code should be 401

    # Verification: Error response
    And the response body should contain field "error"
    And the response body field "error" should equal "登入資料已過期"
