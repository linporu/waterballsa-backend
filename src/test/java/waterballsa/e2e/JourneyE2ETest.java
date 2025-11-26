package waterballsa.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

/**
 * E2E tests for Journey-related endpoints.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>GET /journeys - Get journey list
 * </ul>
 */
@Sql(
    scripts = {"/test-data/cleanup.sql", "/test-data/journeys.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/test-data/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class JourneyE2ETest extends BaseE2ETest {

  // ==================== GET /journeys Tests ====================

  @Test
  @DisplayName("Should successfully get journey list with all journeys in chronological order")
  void shouldGetJourneyListInChronologicalOrder() {
    given()
        .when()
        .get("/journeys")
        .then()
        .statusCode(200)
        .body("journeys", hasSize(2))
        // First journey (oldest)
        .body("journeys[0].id", equalTo(1))
        .body("journeys[0].slug", equalTo("design-patterns-mastery"))
        .body("journeys[0].title", equalTo("軟體設計模式精通之旅"))
        .body("journeys[0].description", equalTo("用 C.A. 模式大大提昇系統思維能力"))
        .body("journeys[0].coverImageUrl", equalTo("https://example.com/cover1.jpg"))
        .body("journeys[0].teacherName", equalTo("水球潘"))
        // Second journey (newer)
        .body("journeys[1].id", equalTo(2))
        .body("journeys[1].slug", equalTo("spring-boot-complete"))
        .body("journeys[1].title", equalTo("Spring Boot 完全攻略"))
        .body("journeys[1].description", equalTo("從零開始學習 Spring Boot 開發"))
        .body("journeys[1].coverImageUrl", equalTo("https://example.com/cover2.jpg"))
        .body("journeys[1].teacherName", equalTo("John Doe"));
  }

  @Test
  @DisplayName("Should not include soft-deleted journeys in the list")
  void shouldNotIncludeSoftDeletedJourneys() {
    given()
        .when()
        .get("/journeys")
        .then()
        .statusCode(200)
        .body("journeys", hasSize(2))
        // Verify that journey with id 999 (soft-deleted) is not included
        .body("journeys.id", not(hasItem(999)))
        .body("journeys.slug", not(hasItem("deleted-journey")));
  }

  @Test
  @DisplayName("Should return empty list when all journeys are soft-deleted")
  @Sql(
      scripts = {"/test-data/cleanup.sql", "/test-data/journeys-all-deleted.sql"},
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  void shouldReturnEmptyListWhenAllJourneysAreDeleted() {
    given()
        .when()
        .get("/journeys")
        .then()
        .statusCode(200)
        .body("journeys", hasSize(0))
        .body("journeys", empty());
  }

  @Test
  @DisplayName("Should verify all required fields are present in journey list items")
  void shouldVerifyAllRequiredFieldsArePresent() {
    given()
        .when()
        .get("/journeys")
        .then()
        .statusCode(200)
        .body("journeys[0].id", notNullValue())
        .body("journeys[0].slug", notNullValue())
        .body("journeys[0].title", notNullValue())
        .body("journeys[0].description", notNullValue())
        .body("journeys[0].coverImageUrl", notNullValue())
        .body("journeys[0].teacherName", notNullValue());
  }
}
