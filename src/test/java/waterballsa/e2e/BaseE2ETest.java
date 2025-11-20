package waterballsa.e2e;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for all E2E tests. This class provides:
 *
 * <ul>
 *   <li>PostgreSQL container setup using Testcontainers
 *   <li>REST Assured configuration
 *   <li>Common utility methods for testing
 * </ul>
 *
 * <p>All E2E test classes should extend this base class to inherit the common setup.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseE2ETest {

  @Container
  @SuppressWarnings("resource") // Container lifecycle managed by Testcontainers
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("e2e_test")
          .withUsername("test")
          .withPassword("test")
          .withReuse(true); // Reuse container across test classes for faster execution

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
  }

  @LocalServerPort protected int port;

  @BeforeEach
  void setUpRestAssured() {
    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @AfterEach
  void cleanUp() {
    RestAssured.reset();
  }

  /**
   * Helper method to login and retrieve authentication token.
   *
   * @param username user username
   * @param password user password
   * @return JWT authentication token
   */
  protected String loginAndGetToken(String username, String password) {
    return given()
        .contentType(ContentType.JSON)
        .body(
            String.format(
                """
                    {
                        "username": "%s",
                        "password": "%s"
                    }
                    """,
                username, password))
        .when()
        .post("/auth/login")
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .getString("accessToken");
  }

  /**
   * Helper method to register a new user.
   *
   * @param username user username
   * @param password user password
   * @return user ID
   */
  protected Long registerUser(String username, String password) {
    return given()
        .contentType(ContentType.JSON)
        .body(
            String.format(
                """
                    {
                        "username": "%s",
                        "password": "%s"
                    }
                    """,
                username, password))
        .when()
        .post("/auth/register")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("userId");
  }

  /**
   * Helper method to create authorization header with Bearer token.
   *
   * @param token JWT token
   * @return formatted authorization header value
   */
  protected String bearerToken(String token) {
    return "Bearer " + token;
  }
}
