package waterballsa.bdd.steps;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
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
   * Send an HTTP request with a JSON body.
   *
   * @param method HTTP method (GET, POST, PUT, DELETE)
   * @param endpoint API endpoint path
   * @param body JSON request body
   */
  @When("I send {string} request to {string} with body:")
  public void sendRequestWithBody(String method, String endpoint, String body) {
    // Replace any variables in the body (e.g., {{token}})
    String processedBody = world.replaceVariables(body);

    // Send the request and store the response
    Response response =
        given().contentType(ContentType.JSON).body(processedBody).request(method, endpoint);

    world.setLastResponse(response);
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
   * Verify that a field in the response body equals an expected value.
   *
   * @param fieldPath JSON path to the field
   * @param expectedValue expected value as string
   */
  @And("the response body field {string} should equal {string}")
  public void verifyFieldEquals(String fieldPath, String expectedValue) {
    // Try to parse as number first, otherwise treat as string
    try {
      int numericValue = Integer.parseInt(expectedValue);
      world.getLastResponse().then().body(fieldPath, equalTo(numericValue));
    } catch (NumberFormatException e) {
      world.getLastResponse().then().body(fieldPath, equalTo(expectedValue));
    }
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
}
