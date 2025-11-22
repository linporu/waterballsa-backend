package waterballsa.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

/**
 * E2E tests for Mission-related endpoints.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>GET /journeys/{journeyId} - Get journey details with chapters and missions
 *   <li>GET /journeys/{journeyId}/missions/{missionId} - Get mission details
 * </ul>
 */
@Sql(scripts = "/test-data/missions.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/test-data/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MissionE2ETest extends BaseE2ETest {

  private String userToken;

  @BeforeEach
  void setUp() {
    String username = "testuser_" + System.currentTimeMillis();
    String password = "Test1234!";
    registerUser(username, password);
    userToken = loginAndGetToken(username, password);
  }

  // ==================== GET /journeys/{journeyId} Tests ====================

  @Test
  @DisplayName("Should successfully get journey details with chapters and missions")
  void shouldGetJourneyDetailsWithChaptersAndMissions() {
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/journeys/1")
        .then()
        .statusCode(200)
        .body("id", equalTo(1))
        .body("slug", equalTo("design-patterns-mastery"))
        .body("title", equalTo("軟體設計模式精通之旅"))
        .body("description", equalTo("用 C.A. 模式大大提昇系統思維能力"))
        .body("coverImageUrl", equalTo("https://example.com/cover1.jpg"))
        .body("teacherName", equalTo("水球潘"))
        .body("chapters", hasSize(3)) // Should only return non-deleted chapters
        .body("chapters[0].id", equalTo(1))
        .body("chapters[0].title", equalTo("課程介紹"))
        .body("chapters[0].orderIndex", equalTo(1))
        .body("chapters[0].missions", hasSize(2))
        .body("chapters[0].missions[0].id", equalTo(1))
        .body("chapters[0].missions[0].type", equalTo("VIDEO"))
        .body("chapters[0].missions[0].title", containsString("課程介紹"))
        .body("chapters[0].missions[0].accessLevel", equalTo("PUBLIC"))
        .body("chapters[0].missions[0].orderIndex", equalTo(1))
        .body("chapters[1].orderIndex", equalTo(2))
        .body("chapters[2].orderIndex", equalTo(3));
  }

  @Test
  @DisplayName("Should return 404 when journey does not exist")
  void shouldReturn404WhenJourneyDoesNotExist() {
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/journeys/99999")
        .then()
        .statusCode(404)
        .body("error", equalTo("查無此旅程"));
  }

  @Test
  @DisplayName("Should return 404 when journey is soft deleted")
  void shouldReturn404WhenJourneyIsSoftDeleted() {
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/journeys/999")
        .then()
        .statusCode(404)
        .body("error", equalTo("查無此旅程"));
  }

  @Test
  @DisplayName("Should not include deleted chapters and missions in journey response")
  void shouldNotIncludeDeletedChaptersAndMissions() {
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/journeys/1")
        .then()
        .statusCode(200)
        .body("chapters", hasSize(3)) // Should not include deleted chapter 999
        .body("chapters[0].missions", hasSize(2)); // Should not include deleted mission 999
  }

  // ==================== GET /journeys/{journeyId}/missions/{missionId} Tests ====================

  @Test
  @DisplayName("Should allow unauthenticated access to PUBLIC mission")
  void shouldAllowUnauthenticatedAccessToPublicMission() {
    given()
        .when()
        .get("/journeys/1/missions/1")
        .then()
        .statusCode(200)
        .body("id", equalTo(1))
        .body("chapterId", equalTo(1))
        .body("journeyId", equalTo(1))
        .body("type", equalTo("VIDEO"))
        .body("title", containsString("課程介紹"))
        .body("description", containsString("Christopher Alexander"))
        .body("accessLevel", equalTo("PUBLIC"))
        .body("createdAt", notNullValue())
        .body("videoLength", equalTo("04:16"))
        .body("reward.exp", equalTo(100))
        .body("content", hasSize(1))
        .body("content[0].id", equalTo(1))
        .body("content[0].type", equalTo("video"))
        .body("content[0].url", containsString("c8m1-0.m3u8"))
        .body("content[0].durationSeconds", equalTo(256));
  }

  @Test
  @DisplayName("Should require authentication for PURCHASED mission")
  void shouldRequireAuthenticationForPurchasedMission() {
    given()
        .when()
        .get("/journeys/1/missions/3")
        .then()
        .statusCode(401)
        .body("error", equalTo("登入資料已過期"));
  }

  @Test
  @DisplayName("Should allow authenticated user to access PURCHASED mission")
  void shouldAllowAuthenticatedUserToAccessPurchasedMission() {
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/journeys/1/missions/3")
        .then()
        .statusCode(200)
        .body("id", equalTo(3))
        .body("chapterId", equalTo(2))
        .body("journeyId", equalTo(1))
        .body("type", equalTo("ARTICLE"))
        .body("title", equalTo("UML 不是英文縮寫字"))
        .body("description", equalTo("UML 統一塑模語言完整介紹"))
        .body("accessLevel", equalTo("PURCHASED"))
        .body("reward.exp", equalTo(100))
        .body("content", hasSize(1))
        .body("content[0].type", equalTo("article"))
        .body("content[0].url", containsString("uml-intro.html"));
  }

  @Test
  @DisplayName("Should return correct data for video mission")
  void shouldReturnCorrectDataForVideoMission() {
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/journeys/1/missions/4")
        .then()
        .statusCode(200)
        .body("id", equalTo(4))
        .body("type", equalTo("VIDEO"))
        .body("videoLength", equalTo("07:00")) // 420 seconds = 7 minutes
        .body("content[0].durationSeconds", equalTo(420));
  }

  @Test
  @DisplayName("Should return correct data for questionnaire mission")
  void shouldReturnCorrectDataForQuestionnaireMission() {
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/journeys/1/missions/6")
        .then()
        .statusCode(200)
        .body("id", equalTo(6))
        .body("type", equalTo("QUESTIONNAIRE"))
        .body("content[0].type", equalTo("form"))
        .body("content[0].url", containsString("feedback-form"));
  }

  @Test
  @DisplayName("Should return 404 when mission does not exist")
  void shouldReturn404WhenMissionDoesNotExist() {
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/journeys/1/missions/99999")
        .then()
        .statusCode(404)
        .body("error", equalTo("查無此任務"));
  }

  @Test
  @DisplayName("Should return 404 when mission is soft deleted")
  void shouldReturn404WhenMissionIsSoftDeleted() {
    given()
        .when()
        .get("/journeys/1/missions/999")
        .then()
        .statusCode(404)
        .body("error", equalTo("查無此任務"));
  }

  @Test
  @DisplayName("Should return 404 when journeyId and missionId do not match")
  void shouldReturn404WhenJourneyIdAndMissionIdDoNotMatch() {
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/journeys/2/missions/1") // Mission 1 belongs to Journey 1, not Journey 2
        .then()
        .statusCode(404)
        .body("error", equalTo("查無此任務"));
  }

  @Test
  @DisplayName("Should allow authenticated user to access AUTHENTICATED level mission")
  void shouldAllowAuthenticatedUserToAccessAuthenticatedLevelMission() {
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/journeys/2/missions/7")
        .then()
        .statusCode(200)
        .body("id", equalTo(7))
        .body("type", equalTo("VIDEO"))
        .body("title", equalTo("Spring Boot 介紹"));
  }

  @Test
  @DisplayName("Should not allow unauthenticated access to AUTHENTICATED level mission")
  void shouldNotAllowUnauthenticatedAccessToAuthenticatedLevelMission() {
    given()
        .when()
        .get("/journeys/2/missions/7")
        .then()
        .statusCode(401)
        .body("error", equalTo("登入資料已過期"));
  }
}
