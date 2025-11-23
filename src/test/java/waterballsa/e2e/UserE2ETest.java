package waterballsa.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E2E tests for user profile endpoints.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>Get current user profile (/users/me)
 *   <li>Authentication and authorization scenarios
 * </ul>
 */
class UserE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Should successfully get authenticated user profile")
  void shouldGetAuthenticatedUserProfile() {
    String username = "testuser_" + System.currentTimeMillis();
    String password = "Test1234!";

    // Register and login to get token
    Long userId = registerUser(username, password);
    String token = loginAndGetToken(username, password);

    // Get user profile with valid token
    given()
        .header("Authorization", bearerToken(token))
        .when()
        .get("/users/me")
        .then()
        .statusCode(200)
        .body("id", equalTo(userId.intValue()))
        .body("username", equalTo(username))
        .body("experiencePoints", equalTo(0))
        .body("level", equalTo(1))
        .body("role", equalTo("STUDENT"));
  }

  @Test
  @DisplayName("Should fail to get profile without Authorization header")
  void shouldFailToGetProfileWithoutAuthorizationHeader() {
    // Attempt to get profile without Authorization header
    given().when().get("/users/me").then().statusCode(401).body("error", notNullValue());
  }

  @Test
  @DisplayName("Should fail to get profile with invalid token")
  void shouldFailToGetProfileWithInvalidToken() {
    String invalidToken = "invalid.jwt.token";

    // Attempt to get profile with invalid token
    given()
        .header("Authorization", bearerToken(invalidToken))
        .when()
        .get("/users/me")
        .then()
        .statusCode(401)
        .body("error", notNullValue());
  }

  @Test
  @DisplayName("Should fail to get profile with logged out token")
  void shouldFailToGetProfileWithLoggedOutToken() {
    String username = "testuser_" + System.currentTimeMillis();
    String password = "Test1234!";

    // Register and login to get token
    registerUser(username, password);
    String token = loginAndGetToken(username, password);

    // Logout to invalidate the token
    given()
        .header("Authorization", bearerToken(token))
        .when()
        .post("/auth/logout")
        .then()
        .statusCode(200);

    // Attempt to get profile with logged out token
    given()
        .header("Authorization", bearerToken(token))
        .when()
        .get("/users/me")
        .then()
        .statusCode(401)
        .body("error", equalTo("登入資料已過期"));
  }
}
