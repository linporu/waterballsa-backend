package waterballsa.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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

  @Autowired private JdbcTemplate jdbcTemplate;

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

  // ==================== GET /journeys/{journeyId} Tests ====================

  @Test
  @DisplayName("Should return journey detail with null userStatus for unauthenticated user")
  @Sql(
      scripts = {"/test-data/cleanup.sql", "/test-data/journey-detail-with-user-status.sql"},
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  void shouldReturnJourneyDetailWithNullUserStatusForUnauthenticatedUser() {
    given()
        .when()
        .get("/journeys/1")
        .then()
        .statusCode(200)
        .body("id", equalTo(1))
        .body("slug", equalTo("design-patterns-mastery"))
        .body("title", equalTo("軟體設計模式精通之旅"))
        .body("userStatus", nullValue())
        .body("chapters", hasSize(2))
        .body("chapters[0].missions[0].status", nullValue());
  }

  @Test
  @DisplayName("Should return journey detail with userStatus for authenticated user who purchased")
  @Sql(
      scripts = {"/test-data/cleanup.sql", "/test-data/journey-detail-with-user-status.sql"},
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  void shouldReturnJourneyDetailWithUserStatusForAuthenticatedUserWhoPurchased() {
    // Create user
    String username = "user_purchased_" + System.currentTimeMillis();
    String password = "Test1234!";
    Long userId = registerUser(username, password);
    String token = loginAndGetToken(username, password);

    // Create paid order and user_journey
    jdbcTemplate.update(
        "INSERT INTO orders (order_number, user_id, status, original_price, discount, price, created_at, updated_at, paid_at) "
            + "VALUES (?, ?, 'PAID', 1999.00, 0.00, 1999.00, NOW(), NOW(), NOW())",
        "ORD-TEST-" + System.currentTimeMillis(),
        userId);

    Long orderId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM orders", Long.class);

    jdbcTemplate.update(
        "INSERT INTO order_items (order_id, journey_id, quantity, original_price, discount, price, created_at) "
            + "VALUES (?, 1, 1, 1999.00, 0.00, 1999.00, NOW())",
        orderId);

    jdbcTemplate.update(
        "INSERT INTO user_journeys (user_id, journey_id, order_id, purchased_at, created_at) "
            + "VALUES (?, 1, ?, NOW(), NOW())",
        userId,
        orderId);

    given()
        .header("Authorization", bearerToken(token))
        .when()
        .get("/journeys/1")
        .then()
        .statusCode(200)
        .body("id", equalTo(1))
        .body("userStatus.hasPurchased", equalTo(true))
        .body("userStatus.hasUnpaidOrder", equalTo(false))
        .body("userStatus.unpaidOrderId", nullValue());
  }

  @Test
  @DisplayName(
      "Should return journey detail with userStatus for authenticated user with unpaid order")
  @Sql(
      scripts = {"/test-data/cleanup.sql", "/test-data/journey-detail-with-user-status.sql"},
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  void shouldReturnJourneyDetailWithUserStatusForAuthenticatedUserWithUnpaidOrder() {
    // Create user
    String username = "user_unpaid_" + System.currentTimeMillis();
    String password = "Test1234!";
    Long userId = registerUser(username, password);
    String token = loginAndGetToken(username, password);

    // Create unpaid order
    jdbcTemplate.update(
        "INSERT INTO orders (order_number, user_id, status, original_price, discount, price, created_at, updated_at, expired_at) "
            + "VALUES (?, ?, 'UNPAID', 1999.00, 0.00, 1999.00, NOW(), NOW(), NOW() + INTERVAL '2 days')",
        "ORD-TEST-" + System.currentTimeMillis(),
        userId);

    Long orderId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM orders", Long.class);

    jdbcTemplate.update(
        "INSERT INTO order_items (order_id, journey_id, quantity, original_price, discount, price, created_at) "
            + "VALUES (?, 1, 1, 1999.00, 0.00, 1999.00, NOW())",
        orderId);

    given()
        .header("Authorization", bearerToken(token))
        .when()
        .get("/journeys/1")
        .then()
        .statusCode(200)
        .body("id", equalTo(1))
        .body("userStatus.hasPurchased", equalTo(false))
        .body("userStatus.hasUnpaidOrder", equalTo(true))
        .body("userStatus.unpaidOrderId", equalTo(orderId.intValue()));
  }

  @Test
  @DisplayName(
      "Should return journey detail with userStatus for authenticated user who has not purchased")
  @Sql(
      scripts = {"/test-data/cleanup.sql", "/test-data/journey-detail-with-user-status.sql"},
      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  void shouldReturnJourneyDetailWithUserStatusForAuthenticatedUserWhoHasNotPurchased() {
    // Create user (no orders)
    String username = "user_not_purchased_" + System.currentTimeMillis();
    String password = "Test1234!";
    registerUser(username, password);
    String token = loginAndGetToken(username, password);

    given()
        .header("Authorization", bearerToken(token))
        .when()
        .get("/journeys/1")
        .then()
        .statusCode(200)
        .body("id", equalTo(1))
        .body("userStatus.hasPurchased", equalTo(false))
        .body("userStatus.hasUnpaidOrder", equalTo(false))
        .body("userStatus.unpaidOrderId", nullValue());
  }
}
