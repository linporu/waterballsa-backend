package waterballsa.bdd.support;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * REST Assured configuration for Cucumber tests.
 *
 * <p>This class configures REST Assured before each scenario and cleans up after.
 */
public class RestAssuredConfig {

  @LocalServerPort private int port;

  @Autowired private World world;

  /** Configure REST Assured before each scenario. */
  @Before
  public void setUp() {
    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  /** Clean up after each scenario. */
  @After
  public void tearDown() {
    world.reset();
    RestAssured.reset();
  }
}
