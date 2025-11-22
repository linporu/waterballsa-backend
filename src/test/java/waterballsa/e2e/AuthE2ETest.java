package waterballsa.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E2E tests for authentication endpoints.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>User registration (success and failure scenarios)
 *   <li>User login (success and failure scenarios)
 *   <li>User logout (success and failure scenarios)
 * </ul>
 */
class AuthE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Should successfully register a new user")
  void shouldRegisterNewUser() {
    String username = "testuser_" + System.currentTimeMillis();
    String password = "Test1234!";

    given()
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
        .body("message", equalTo("Registration successful"))
        .body("userId", greaterThan(0));
  }

  @Test
  @DisplayName("Should fail to register with duplicate username")
  void shouldFailToRegisterWithDuplicateUsername() {
    String username = "testuser_" + System.currentTimeMillis();
    String password = "Test1234!";

    // First registration succeeds
    registerUser(username, password);

    // Second registration with same username should fail
    given()
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
        .statusCode(409)
        .body("error", equalTo("使用者名稱已存在"));
  }

  @Test
  @DisplayName("Should successfully login with valid credentials")
  void shouldLoginWithValidCredentials() {
    String username = "testuser_" + System.currentTimeMillis();
    String password = "Test1234!";

    // Register user first
    Long userId = registerUser(username, password);

    // Login with valid credentials
    given()
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
        .body("accessToken", notNullValue())
        .body("user.id", equalTo(userId.intValue()))
        .body("user.username", equalTo(username))
        .body("user.experience", equalTo(0));
  }

  @Test
  @DisplayName("Should fail to login with wrong password")
  void shouldFailToLoginWithWrongPassword() {
    String username = "testuser_" + System.currentTimeMillis();
    String password = "Test1234!";
    String wrongPassword = "WrongPassword123!";

    // Register user first
    registerUser(username, password);

    // Attempt login with wrong password
    given()
        .contentType(ContentType.JSON)
        .body(
            String.format(
                """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """,
                username, wrongPassword))
        .when()
        .post("/auth/login")
        .then()
        .statusCode(401)
        .body("error", equalTo("帳號或密碼錯誤"));
  }

  @Test
  @DisplayName("Should fail to login with non-existent username")
  void shouldFailToLoginWithNonExistentUsername() {
    String nonExistentUsername = "nonexistent_" + System.currentTimeMillis();
    String password = "Test1234!";

    // Attempt login with non-existent username
    given()
        .contentType(ContentType.JSON)
        .body(
            String.format(
                """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """,
                nonExistentUsername, password))
        .when()
        .post("/auth/login")
        .then()
        .statusCode(401)
        .body("error", equalTo("帳號或密碼錯誤"));
  }

  @Test
  @DisplayName("Should successfully logout with valid token")
  void shouldLogoutWithValidToken() {
    String username = "testuser_" + System.currentTimeMillis();
    String password = "Test1234!";

    // Register and login to get token
    registerUser(username, password);
    String token = loginAndGetToken(username, password);

    // Logout with valid token
    given()
        .header("Authorization", bearerToken(token))
        .when()
        .post("/auth/logout")
        .then()
        .statusCode(200)
        .body("message", equalTo("Logout successful"));
  }

  @Test
  @DisplayName("Should fail to logout without Authorization header")
  void shouldFailToLogoutWithoutAuthorizationHeader() {
    // Attempt logout without Authorization header
    given()
        .when()
        .post("/auth/logout")
        .then()
        .statusCode(401)
        .body("error", equalTo("登入資料已過期"));
  }

  @Test
  @DisplayName("Should fail to logout twice with same token")
  void shouldFailToLogoutTwiceWithSameToken() {
    String username = "testuser_" + System.currentTimeMillis();
    String password = "Test1234!";

    // Register and login to get token
    registerUser(username, password);
    String token = loginAndGetToken(username, password);

    // First logout succeeds
    given()
        .header("Authorization", bearerToken(token))
        .when()
        .post("/auth/logout")
        .then()
        .statusCode(200)
        .body("message", equalTo("Logout successful"));

    // Second logout with same token should fail
    given()
        .header("Authorization", bearerToken(token))
        .when()
        .post("/auth/logout")
        .then()
        .statusCode(401)
        .body("error", equalTo("登入資料已過期"));
  }
}
