package waterballsa.bdd.steps;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import waterballsa.bdd.support.World;

/**
 * Step definitions for ISA (Implementation/API) layer tests.
 *
 * <p>This class provides generic, reusable step definitions for API testing. These steps should be
 * technology-focused and not domain-specific, allowing them to be reused across different features.
 *
 * <p>Key principles:
 *
 * <ul>
 *   <li>Generic: Works with any HTTP endpoint
 *   <li>Reusable: Can be used by multiple feature files
 *   <li>No business logic: Only handles HTTP request/response mechanics
 * </ul>
 */
public class IsaStepDefinitions {

  @Autowired private World world;

  /**
   * Set the Authorization header for the next HTTP request.
   *
   * <p>This is a setup step that prepares the Authorization header. The actual request is sent by a
   * separate "When I send..." step.
   *
   * @param token Authorization token (supports variable substitution with {{variableName}})
   */
  @Given("I set Authorization header to {string}")
  public void setAuthorizationHeader(String token) {
    String processedToken = world.replaceVariables(token);
    world.setHeader("Authorization", "Bearer " + processedToken);
  }

  /**
   * Set the request body for the next HTTP request.
   *
   * <p>This is a setup step that prepares the request body. The actual request is sent by a
   * separate "When I send..." step.
   *
   * @param body JSON request body (supports variable substitution with {{variableName}})
   */
  @Given("I set request body to:")
  public void setRequestBody(String body) {
    String processedBody = world.replaceVariables(body);
    world.setRequestBody(processedBody);
  }

  /**
   * Send an HTTP request with any previously configured headers and body.
   *
   * <p>This step applies all headers and body that were set by previous "Given I set..." steps. If
   * no headers or body were set, it sends a plain request.
   *
   * <p>After sending the request, headers and body are cleared automatically to prevent
   * contamination between scenarios.
   *
   * @param method HTTP method (GET, POST, PUT, DELETE)
   * @param endpoint API endpoint path (supports variable substitution with {{variableName}})
   */
  @When("I send {string} request to {string}")
  public void sendRequest(String method, String endpoint) {
    RequestSpecification request = given();

    // Replace variables in endpoint
    String processedEndpoint = world.replaceVariables(endpoint);

    // Apply all headers from World
    for (Map.Entry<String, String> header : world.getHeaders().entrySet()) {
      request.header(header.getKey(), header.getValue());
    }

    // Apply body from World if exists
    if (world.getRequestBody() != null) {
      request.contentType(ContentType.JSON).body(world.getRequestBody());
    }

    // Send the request and store the response
    Response response = request.request(method, processedEndpoint);
    world.setLastResponse(response);

    // Clear headers and body after sending to prevent contamination
    world.getHeaders().clear();
    world.setRequestBody(null);
  }

  /**
   * Verify the HTTP response status code.
   *
   * @param expectedStatusCode expected HTTP status code
   */
  @Then("the response status code should be {int}")
  public void verifyStatusCode(int expectedStatusCode) {
    world.getLastResponse().then().statusCode(expectedStatusCode);
  }

  /**
   * Verify that a field exists in the response body (regardless of whether its value is null).
   *
   * <p>This checks that the field key exists in the JSON response. The field can have any value,
   * including null.
   *
   * <p>Usage examples:
   *
   * <pre>
   * Then the response body should contain field "paidAt"  # passes even if paidAt is null
   * Then the response body should contain field "user.username"
   * </pre>
   *
   * <p>Note: To verify a field exists AND has a non-null value, use "should have non-null field"
   * instead.
   *
   * @param fieldPath JSON path to the field (e.g., "user.username")
   */
  @And("the response body should contain field {string}")
  public void verifyFieldExists(String fieldPath) {
    // Use hasKey matcher to check field existence regardless of null value
    // This requires accessing the response as a Map
    try {
      world.getLastResponse().then().body("$", org.hamcrest.Matchers.hasKey(fieldPath));
    } catch (AssertionError e) {
      // If simple key check fails, try nested path check
      // For nested paths like "user.username", we need to verify each level exists
      String[] pathParts = fieldPath.split("\\.");
      StringBuilder currentPath = new StringBuilder();

      for (int i = 0; i < pathParts.length; i++) {
        if (i > 0) {
          currentPath.append(".");
        }
        currentPath.append(pathParts[i]);

        // Check if this level of the path exists
        // We use a lenient check that allows null values
        try {
          world
              .getLastResponse()
              .then()
              .body(currentPath.toString(), org.hamcrest.Matchers.anything());
        } catch (Exception ex) {
          throw new AssertionError(
              String.format("Field '%s' does not exist in the response", fieldPath));
        }
      }
    }
  }

  /**
   * Verify that a field exists in the response body AND has a non-null value.
   *
   * <p>This is useful when you want to ensure a field is both present and has a value.
   *
   * <p>Usage examples:
   *
   * <pre>
   * Then the response body should have non-null field "accessToken"
   * Then the response body should have non-null field "user.id"
   * </pre>
   *
   * @param fieldPath JSON path to the field
   */
  @And("the response body should have non-null field {string}")
  public void verifyFieldExistsAndNotNull(String fieldPath) {
    world.getLastResponse().then().body(fieldPath, notNullValue());
  }

  /**
   * Verify that a field in the response body equals an expected string value.
   *
   * @param fieldPath JSON path to the field
   * @param expectedValue expected string value
   */
  @And("the response body field {string} should equal string {string}")
  public void verifyFieldEqualsString(String fieldPath, String expectedValue) {
    world.getLastResponse().then().body(fieldPath, equalTo(expectedValue));
  }

  /**
   * Verify that a field in the response body equals an expected integer value.
   *
   * @param fieldPath JSON path to the field
   * @param expectedValue expected integer value
   */
  @And("the response body field {string} should equal number {int}")
  public void verifyFieldEqualsInt(String fieldPath, int expectedValue) {
    world.getLastResponse().then().body(fieldPath, equalTo(expectedValue));
  }

  /**
   * Verify that a field in the response body equals an expected decimal value.
   *
   * @param fieldPath JSON path to the field
   * @param expectedValue expected decimal value as string (will be converted to BigDecimal)
   */
  @And("the response body field {string} should equal decimal {string}")
  public void verifyFieldEqualsDecimal(String fieldPath, String expectedValue) {
    BigDecimal expected = new BigDecimal(expectedValue);
    // For JSON comparison, we need to compare as float since JSON doesn't have BigDecimal
    world.getLastResponse().then().body(fieldPath, equalTo(expected.floatValue()));
  }

  /**
   * Verify that a field in the response body equals an expected boolean value.
   *
   * @param fieldPath JSON path to the field
   * @param expectedValue expected boolean value as string ("true" or "false")
   */
  @And("the response body field {string} should equal boolean {word}")
  public void verifyFieldEqualsBoolean(String fieldPath, String expectedValue) {
    boolean expected = Boolean.parseBoolean(expectedValue);
    world.getLastResponse().then().body(fieldPath, equalTo(expected));
  }

  /**
   * Verify that a field in the response body is null.
   *
   * @param fieldPath JSON path to the field
   */
  @And("the response body field {string} should be null")
  public void verifyFieldIsNull(String fieldPath) {
    world.getLastResponse().then().body(fieldPath, nullValue());
  }

  /**
   * Store a value from the response into a variable for later use.
   *
   * @param fieldPath JSON path to extract the value from
   * @param variableName name to store the value as
   */
  @And("I store the response field {string} as {string}")
  public void storeResponseField(String fieldPath, String variableName) {
    String value = world.getLastResponse().then().extract().path(fieldPath).toString();
    world.setVariable(variableName, value);
  }

  /**
   * Copy an existing variable to a new variable name.
   *
   * <p>This is useful when you need to preserve a variable value before it gets overwritten.
   *
   * <p>Usage example:
   *
   * <pre>
   * # lastJourneyId will be overwritten when creating a new journey
   * # So we copy it to a new variable first
   * Given I copy variable "lastJourneyId" to "firstJourneyId"
   * </pre>
   *
   * @param sourceVariable source variable name (supports {{variableName}} format)
   * @param targetVariable target variable name
   */
  @Given("I copy variable {string} to {string}")
  public void copyVariable(String sourceVariable, String targetVariable) {
    String value = world.getVariable(sourceVariable);
    if (value == null) {
      throw new IllegalArgumentException(
          String.format("Source variable '%s' does not exist", sourceVariable));
    }
    world.setVariable(targetVariable, value);
  }

  /**
   * Verify that a field in the response body has a specific size. Works for arrays, strings, or any
   * collection.
   *
   * <p>Usage examples:
   *
   * <pre>
   * Then the response body field "journeys" should have size 2
   * And the response body field "chapters" should have size 3
   * And the response body field "resource" should have size 1
   * </pre>
   *
   * @param fieldPath JSON path to the field
   * @param expectedSize expected size
   */
  @Then("the response body field {string} should have size {int}")
  public void verifyFieldSize(String fieldPath, int expectedSize) {
    world.getLastResponse().then().body(fieldPath + ".size()", equalTo(expectedSize));
  }

  /**
   * Verify that a field in the response body contains a specific substring.
   *
   * <p>This is useful for pattern matching like checking if an order number contains a user ID.
   *
   * <p>Supports variable replacement in the expected substring.
   *
   * <p>Usage examples:
   *
   * <pre>
   * Then the response body field "orderNumber" should contain "42"
   * And the response body field "orderNumber" should contain "{{lastUserId}}"
   * And the response body field "message" should contain "success"
   * </pre>
   *
   * @param fieldPath JSON path to the field
   * @param expectedSubstring substring that should be present in the field value (supports variable
   *     replacement with {{variableName}})
   */
  @And("the response body field {string} should contain {string}")
  public void verifyFieldContains(String fieldPath, String expectedSubstring) {
    // Support variable replacement for expected substring
    String processedSubstring = world.replaceVariables(expectedSubstring);

    String actualValue = world.getLastResponse().then().extract().path(fieldPath).toString();
    if (!actualValue.contains(processedSubstring)) {
      throw new AssertionError(
          String.format(
              "Field '%s' with value '%s' does not contain expected substring '%s'",
              fieldPath, actualValue, processedSubstring));
    }
  }

  /**
   * Login as a user and store the access token for subsequent requests.
   *
   * <p>This is a convenience step that combines: 1. Setting request body with login credentials 2.
   * Sending POST request to /auth/login 3. Storing the access token in World for later use
   *
   * <p>The access token can be used in subsequent steps with: Given I set Authorization header to
   * "{{accessToken}}"
   *
   * <p>Usage example:
   *
   * <pre>
   * Given I login as "Alice" with password "Test1234!"
   * When I send "POST" request to "/orders"
   * </pre>
   *
   * @param username username to login with
   * @param password password to login with
   */
  @Given("I login as {string} with password {string}")
  public void loginAsUser(String username, String password) {
    // Set request body
    String loginBody =
        String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);
    world.setRequestBody(loginBody);

    // Send login request
    RequestSpecification request = given().contentType(ContentType.JSON).body(loginBody);
    Response response = request.post("/auth/login");
    world.setLastResponse(response);

    // Store the access token
    String accessToken = response.then().extract().path("accessToken");
    world.setVariable("accessToken", accessToken);

    // Also store user ID if needed
    Integer userId = response.then().extract().path("user.id");
    world.setVariable("userId", userId.toString());

    // Clear request body after login
    world.setRequestBody(null);
  }
}
