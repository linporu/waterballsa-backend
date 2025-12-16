package waterballsa.bdd.support;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * World object for sharing state between Cucumber step definitions.
 *
 * <p>This class acts as a context/state holder that can be injected into step definition classes.
 * It stores:
 *
 * <ul>
 *   <li>Last HTTP response received
 *   <li>Variables for dynamic data (e.g., tokens, IDs)
 *   <li>HTTP request headers for the next request
 *   <li>HTTP request body for the next request
 *   <li>Test data created during scenario execution
 * </ul>
 *
 * <p>Spring manages this as a scenario-scoped bean, ensuring isolation between scenarios.
 */
@Component
public class World {

  private Response lastResponse;
  private final Map<String, String> variables = new HashMap<>();
  private final Map<String, String> headers = new HashMap<>();
  private String requestBody;

  /**
   * Get the last HTTP response received.
   *
   * @return the last response
   */
  public Response getLastResponse() {
    return lastResponse;
  }

  /**
   * Set the last HTTP response.
   *
   * @param response the response to store
   */
  public void setLastResponse(Response response) {
    this.lastResponse = response;
  }

  /**
   * Store a variable for later use.
   *
   * @param key variable name
   * @param value variable value
   */
  public void setVariable(String key, String value) {
    variables.put(key, value);
  }

  /**
   * Get a stored variable.
   *
   * @param key variable name
   * @return variable value, or null if not found
   */
  public String getVariable(String key) {
    return variables.get(key);
  }

  /**
   * Replace variables in a string with their stored values.
   *
   * <p>Variables in the format {{variableName}} will be replaced.
   *
   * @param input string containing variables
   * @return string with variables replaced
   */
  public String replaceVariables(String input) {
    String result = input;
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
    }
    return result;
  }

  /**
   * Get all stored headers for the next request.
   *
   * @return map of header names to values
   */
  public Map<String, String> getHeaders() {
    return headers;
  }

  /**
   * Set a header for the next request.
   *
   * @param name header name
   * @param value header value
   */
  public void setHeader(String name, String value) {
    headers.put(name, value);
  }

  /**
   * Get the request body for the next request.
   *
   * @return request body, or null if not set
   */
  public String getRequestBody() {
    return requestBody;
  }

  /**
   * Set the request body for the next request.
   *
   * @param body request body
   */
  public void setRequestBody(String body) {
    this.requestBody = body;
  }

  /** Clear all stored data (called between scenarios). */
  public void reset() {
    lastResponse = null;
    variables.clear();
    headers.clear();
    requestBody = null;
  }
}
