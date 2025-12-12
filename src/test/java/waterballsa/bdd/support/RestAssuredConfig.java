package waterballsa.bdd.support;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * REST Assured configuration for Cucumber tests.
 *
 * <p>This class configures REST Assured before each scenario and cleans up after.
 */
public class RestAssuredConfig {

  @LocalServerPort private int port;

  @Autowired private World world;
  @Autowired private JdbcTemplate jdbcTemplate;

  /** Configure REST Assured before each scenario. */
  @Before
  public void setUp() {
    // Clean database before each scenario to ensure test isolation
    cleanDatabase();

    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  /**
   * Clean all test data from the database.
   * Deletes in order to respect foreign key constraints.
   */
  private void cleanDatabase() {
    jdbcTemplate.execute("DELETE FROM user_mission_progress WHERE TRUE");
    jdbcTemplate.execute("DELETE FROM order_items WHERE TRUE");
    jdbcTemplate.execute("DELETE FROM user_journeys WHERE TRUE");
    jdbcTemplate.execute("DELETE FROM orders WHERE TRUE");
    jdbcTemplate.execute("DELETE FROM rewards WHERE TRUE");
    jdbcTemplate.execute("DELETE FROM mission_resources WHERE TRUE");
    jdbcTemplate.execute("DELETE FROM missions WHERE TRUE");
    jdbcTemplate.execute("DELETE FROM chapters WHERE TRUE");
    jdbcTemplate.execute("DELETE FROM journeys WHERE TRUE");
    jdbcTemplate.execute("DELETE FROM access_tokens WHERE TRUE");
    jdbcTemplate.execute("DELETE FROM users WHERE TRUE");
  }

  /** Clean up after each scenario. */
  @After
  public void tearDown() {
    world.reset();
    RestAssured.reset();
  }
}
