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
 *   <li>Test data created during scenario execution
 * </ul>
 *
 * <p>Spring manages this as a scenario-scoped bean, ensuring isolation between scenarios.
 */
@Component
public class World {

  private Response lastResponse;
  private final Map<String, String> variables = new HashMap<>();

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

  /** Clear all stored data (called between scenarios). */
  public void reset() {
    lastResponse = null;
    variables.clear();
  }
}
