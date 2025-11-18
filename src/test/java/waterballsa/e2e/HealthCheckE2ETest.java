package waterballsa.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E2E test for health check endpoint.
 *
 * <p>This is a simple E2E test that verifies the health check endpoint is working correctly with
 * the PostgreSQL database.
 */
class HealthCheckE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Health check endpoint should return UP status")
  void shouldReturnHealthyStatus() {
    given()
        .when()
        .get("/healthz")
        .then()
        .statusCode(200)
        .body("status", equalTo("UP"));
  }

  @Test
  @DisplayName("Health check should verify database connection")
  void shouldVerifyDatabaseConnection() {
    given()
        .when()
        .get("/healthz")
        .then()
        .statusCode(200)
        .body("status", equalTo("UP"))
        .body("database", equalTo("UP"));
  }
}
