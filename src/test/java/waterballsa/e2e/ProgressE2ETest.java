package waterballsa.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

/**
 * E2E tests for Progress-related endpoints.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>GET /users/{userId}/missions/{missionId}/progress - Get user's mission progress
 *   <li>PUT /users/{userId}/missions/{missionId}/progress - Update user's mission progress (upsert)
 * </ul>
 */
@Sql(
    scripts = {"/test-data/cleanup.sql", "/test-data/missions.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/test-data/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ProgressE2ETest extends BaseE2ETest {

  private String userToken;
  private Long userId;

  @BeforeEach
  void setUp() {
    String username = "testuser_" + System.currentTimeMillis();
    String password = "Test1234!";
    userId = registerUser(username, password);
    userToken = loginAndGetToken(username, password);
  }

  // ==================== GET /users/{userId}/missions/{missionId}/progress Tests
  // ====================

  @Nested
  @DisplayName("GET /users/{userId}/missions/{missionId}/progress")
  class GetProgressTests {

    @Test
    @DisplayName("Should return default progress when no record exists")
    void shouldReturnDefaultProgressWhenNoRecordExists() {
      // Mission 2 exists but user has no progress record for it
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .get("/users/{userId}/missions/{missionId}/progress", userId, 2)
          .then()
          .statusCode(200)
          .body("missionId", equalTo(2))
          .body("status", equalTo("UNCOMPLETED"))
          .body("watchPositionSeconds", equalTo(0));
    }

    @Test
    @DisplayName("Should return existing progress record")
    void shouldReturnExistingProgress() {
      // First create progress via PUT
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 100}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 1)
          .then()
          .statusCode(200);

      // Then verify GET returns the correct progress
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .get("/users/{userId}/missions/{missionId}/progress", userId, 1)
          .then()
          .statusCode(200)
          .body("missionId", equalTo(1))
          .body("status", equalTo("UNCOMPLETED"))
          .body("watchPositionSeconds", equalTo(100));
    }

    @Test
    @DisplayName("Should return COMPLETED status for completed mission")
    void shouldReturnCompletedStatus() {
      // Mission 1 has duration 256 seconds - complete it first
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 256}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 1)
          .then()
          .statusCode(200);

      // Verify GET returns COMPLETED status
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .get("/users/{userId}/missions/{missionId}/progress", userId, 1)
          .then()
          .statusCode(200)
          .body("missionId", equalTo(1))
          .body("status", equalTo("COMPLETED"))
          .body("watchPositionSeconds", equalTo(256));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() {
      given()
          .when()
          .get("/users/{userId}/missions/{missionId}/progress", userId, 1)
          .then()
          .statusCode(401)
          .body("error", equalTo("登入資料已過期"));
    }

    @Test
    @DisplayName("Should return 403 when accessing other user's progress")
    void shouldReturn403WhenAccessingOtherUserProgress() {
      // Register another user
      String anotherUsername = "another_" + System.currentTimeMillis();
      Long anotherUserId = registerUser(anotherUsername, "Test1234!");

      // Try to access another user's progress with current user's token
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .get("/users/{userId}/missions/{missionId}/progress", anotherUserId, 1)
          .then()
          .statusCode(403)
          .body("error", equalTo("無法存取其他使用者的進度"));
    }

    @Test
    @DisplayName("Should return 404 when mission does not exist")
    void shouldReturn404WhenMissionNotFound() {
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .get("/users/{userId}/missions/{missionId}/progress", userId, 99999)
          .then()
          .statusCode(404)
          .body("error", equalTo("查無此任務"));
    }

    @Test
    @DisplayName("Should return 404 when mission is soft deleted")
    void shouldReturn404WhenMissionIsSoftDeleted() {
      // Mission 999 is soft deleted in missions.sql
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .get("/users/{userId}/missions/{missionId}/progress", userId, 999)
          .then()
          .statusCode(404)
          .body("error", equalTo("查無此任務"));
    }
  }

  // ==================== PUT /users/{userId}/missions/{missionId}/progress Tests
  // ====================

  @Nested
  @DisplayName("PUT /users/{userId}/missions/{missionId}/progress")
  class UpdateProgressTests {

    @Test
    @DisplayName("Should create progress when not exists (upsert)")
    void shouldCreateProgressWhenNotExists() {
      // User has no progress for mission 2, PUT should create it
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 50}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 2)
          .then()
          .statusCode(200)
          .body("missionId", equalTo(2))
          .body("status", equalTo("UNCOMPLETED"))
          .body("watchPositionSeconds", equalTo(50));

      // Verify the progress was created
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .get("/users/{userId}/missions/{missionId}/progress", userId, 2)
          .then()
          .statusCode(200)
          .body("watchPositionSeconds", equalTo(50));
    }

    @Test
    @DisplayName("Should update existing progress")
    void shouldUpdateExistingProgress() {
      // First create progress at 50 seconds
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 50}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 1)
          .then()
          .statusCode(200);

      // Update to 150 seconds
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 150}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 1)
          .then()
          .statusCode(200)
          .body("missionId", equalTo(1))
          .body("status", equalTo("UNCOMPLETED"))
          .body("watchPositionSeconds", equalTo(150));
    }

    @Test
    @DisplayName("Should set status to COMPLETED when reaching duration")
    void shouldSetStatusToCompletedWhenReachingDuration() {
      // Mission 1 has duration 256 seconds
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 256}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 1)
          .then()
          .statusCode(200)
          .body("missionId", equalTo(1))
          .body("status", equalTo("COMPLETED"))
          .body("watchPositionSeconds", equalTo(256));
    }

    @Test
    @DisplayName("Should cap watch position to duration when exceeding")
    void shouldCapWatchPositionToDuration() {
      // Mission 1 has duration 256 seconds, try to set 300
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 300}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 1)
          .then()
          .statusCode(200)
          .body("missionId", equalTo(1))
          .body("status", equalTo("COMPLETED"))
          .body("watchPositionSeconds", equalTo(256)); // Capped to duration
    }

    @Test
    @DisplayName("Should return 400 for ARTICLE mission")
    void shouldReturn400ForArticleMission() {
      // Mission 3 is ARTICLE type
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 100}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 3)
          .then()
          .statusCode(400)
          .body("error", equalTo("此任務類型不支援進度追蹤"));
    }

    @Test
    @DisplayName("Should return 400 for QUESTIONNAIRE mission")
    void shouldReturn400ForQuestionnaireMission() {
      // Mission 6 is QUESTIONNAIRE type
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 100}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 6)
          .then()
          .statusCode(400)
          .body("error", equalTo("此任務類型不支援進度追蹤"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() {
      given()
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 100}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 1)
          .then()
          .statusCode(401)
          .body("error", equalTo("登入資料已過期"));
    }

    @Test
    @DisplayName("Should return 403 when updating other user's progress")
    void shouldReturn403WhenUpdatingOtherUserProgress() {
      // Register another user
      String anotherUsername = "another_" + System.currentTimeMillis();
      Long anotherUserId = registerUser(anotherUsername, "Test1234!");

      // Try to update another user's progress with current user's token
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 100}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", anotherUserId, 1)
          .then()
          .statusCode(403)
          .body("error", equalTo("無法存取其他使用者的進度"));
    }

    @Test
    @DisplayName("Should return 404 when mission does not exist")
    void shouldReturn404WhenMissionNotFound() {
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 100}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 99999)
          .then()
          .statusCode(404)
          .body("error", equalTo("查無此任務"));
    }

    @Test
    @DisplayName("Should return 404 when mission is soft deleted")
    void shouldReturn404WhenMissionIsSoftDeleted() {
      // Mission 999 is soft deleted in missions.sql
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 100}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 999)
          .then()
          .statusCode(404)
          .body("error", equalTo("查無此任務"));
    }

    @Test
    @DisplayName("Should return 400 when watch position is negative")
    void shouldReturn400WhenWatchPositionIsNegative() {
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": -10}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 1)
          .then()
          .statusCode(400)
          .body("error", equalTo("觀看位置不可為負數"));
    }

    @Test
    @DisplayName("Should allow rewatching and updating progress for completed mission")
    void shouldAllowRewatchingCompletedMission() {
      // First complete the mission
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 256}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 1)
          .then()
          .statusCode(200)
          .body("status", equalTo("COMPLETED"));

      // User rewatches from beginning - should still be COMPLETED
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body("{\"watchPositionSeconds\": 50}")
          .when()
          .put("/users/{userId}/missions/{missionId}/progress", userId, 1)
          .then()
          .statusCode(200)
          .body("missionId", equalTo(1))
          .body("status", equalTo("COMPLETED")) // Status should remain COMPLETED
          .body("watchPositionSeconds", equalTo(50));
    }
  }
}
