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
   * Verify that a field exists in the response body.
   *
   * @param fieldPath JSON path to the field (e.g., "user.username")
   */
  @And("the response body should contain field {string}")
  public void verifyFieldExists(String fieldPath) {
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
   * @param variableName name to store the value as
   * @param fieldPath JSON path to extract the value from
   */
  @And("I store the response field {string} as {string}")
  public void storeResponseField(String fieldPath, String variableName) {
    String value = world.getLastResponse().then().extract().path(fieldPath).toString();
    world.setVariable(variableName, value);
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
}
