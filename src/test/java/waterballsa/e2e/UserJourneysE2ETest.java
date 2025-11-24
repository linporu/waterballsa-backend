package waterballsa.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

/**
 * E2E tests for user journeys endpoint.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>GET /users/{userId}/journeys - Get user's purchased journeys
 * </ul>
 */
@Sql(
    scripts = {"/test-data/cleanup.sql", "/test-data/orders.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/test-data/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserJourneysE2ETest extends BaseE2ETest {

  private String userToken;
  private Long userId;

  @BeforeEach
  void setUp() {
    String username = "testuser_" + System.currentTimeMillis();
    String password = "Test1234!";
    userId = registerUser(username, password);
    userToken = loginAndGetToken(username, password);
  }

  @Test
  @DisplayName("Should successfully get user's purchased journeys")
  void shouldGetUserPurchasedJourneysSuccessfully() {
    // Create and pay two orders
    createAndPayOrder(1L);
    createAndPayOrder(2L);

    // Get user's purchased journeys
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/users/{userId}/journeys", userId)
        .then()
        .statusCode(200)
        .body("journeys", hasSize(2))
        .body("journeys[0].journeyId", notNullValue())
        .body("journeys[0].journeyTitle", notNullValue())
        .body("journeys[0].journeySlug", notNullValue())
        .body("journeys[0].coverImageUrl", notNullValue())
        .body("journeys[0].teacherName", notNullValue())
        .body("journeys[0].purchasedAt", notNullValue())
        .body("journeys[0].orderNumber", notNullValue());
  }

  @Test
  @DisplayName("Should get empty journey list when user has no purchases")
  void shouldGetEmptyJourneyList() {
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/users/{userId}/journeys", userId)
        .then()
        .statusCode(200)
        .body("journeys", hasSize(0));
  }

  @Test
  @DisplayName("Should only include journeys from paid orders")
  void shouldOnlyIncludePaidOrders() {
    // Create one unpaid order
    createOrder(1L);

    // Create and pay another order
    createAndPayOrder(2L);

    // Verify only the paid journey appears
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/users/{userId}/journeys", userId)
        .then()
        .statusCode(200)
        .body("journeys", hasSize(1))
        .body("journeys[0].journeyId", equalTo(2));
  }

  @Test
  @DisplayName("Should include correct purchase information")
  void shouldIncludeCorrectPurchaseInfo() {
    // Create and pay order
    Long orderId = createAndPayOrder(1L);

    // Get order details
    Response orderResponse =
        given()
            .header("Authorization", bearerToken(userToken))
            .when()
            .get("/orders/{orderId}", orderId)
            .then()
            .statusCode(200)
            .extract()
            .response();

    String expectedOrderNumber = orderResponse.jsonPath().getString("orderNumber");
    Long expectedPaidAt = orderResponse.jsonPath().getLong("paidAt");
    String expectedJourneyTitle = orderResponse.jsonPath().getString("items[0].journeyTitle");

    // Verify journey list contains correct purchase info
    Response journeyResponse =
        given()
            .header("Authorization", bearerToken(userToken))
            .when()
            .get("/users/{userId}/journeys", userId)
            .then()
            .statusCode(200)
            .body("journeys", hasSize(1))
            .extract()
            .response();

    String actualOrderNumber = journeyResponse.jsonPath().getString("journeys[0].orderNumber");
    Long actualPurchasedAt = journeyResponse.jsonPath().getLong("journeys[0].purchasedAt");
    String actualJourneyTitle = journeyResponse.jsonPath().getString("journeys[0].journeyTitle");

    // Verify purchasedAt equals order's paidAt
    assertThat(actualPurchasedAt, equalTo(expectedPaidAt));
    // Verify orderNumber matches
    assertThat(actualOrderNumber, equalTo(expectedOrderNumber));
    // Verify journey title matches
    assertThat(actualJourneyTitle, equalTo(expectedJourneyTitle));
  }

  @Test
  @DisplayName("Should fail when not authenticated")
  void shouldFailWhenNotAuthenticated() {
    given().when().get("/users/{userId}/journeys", userId).then().statusCode(401);
  }

  @Test
  @DisplayName("Should fail when accessing other user's journeys")
  void shouldFailWhenAccessingOtherUsersJourneys() {
    // Create and pay order for first user
    createAndPayOrder(1L);

    // Register and login as second user
    String secondUsername = "testuser2_" + System.currentTimeMillis();
    String secondPassword = "Test1234!";
    Long secondUserId = registerUser(secondUsername, secondPassword);
    String secondUserToken = loginAndGetToken(secondUsername, secondPassword);

    // Try to access first user's journeys as second user
    given()
        .header("Authorization", bearerToken(secondUserToken))
        .when()
        .get("/users/{userId}/journeys", userId)
        .then()
        .statusCode(404);

    // Verify second user can only access their own journeys
    given()
        .header("Authorization", bearerToken(secondUserToken))
        .when()
        .get("/users/{userId}/journeys", secondUserId)
        .then()
        .statusCode(200)
        .body("journeys", hasSize(0));
  }

  // ==================== Helper Methods ====================

  /**
   * Helper method to create an order for a specific journey.
   *
   * @param journeyId the journey ID to order
   * @return the created order ID
   */
  private Long createOrder(Long journeyId) {
    String requestBody =
        String.format(
            """
                {
                  "items": [
                    {
                      "journeyId": %d,
                      "quantity": 1
                    }
                  ]
                }
                """,
            journeyId);

    return given()
        .header("Authorization", bearerToken(userToken))
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post("/orders")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");
  }

  /**
   * Helper method to create and pay an order for a specific journey.
   *
   * @param journeyId the journey ID to order
   * @return the paid order ID
   */
  private Long createAndPayOrder(Long journeyId) {
    Long orderId = createOrder(journeyId);

    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .post("/orders/{orderId}/action/pay", orderId)
        .then()
        .statusCode(200);

    return orderId;
  }

  // Helper method for assertThat
  private static <T> void assertThat(T actual, org.hamcrest.Matcher<? super T> matcher) {
    org.hamcrest.MatcherAssert.assertThat(actual, matcher);
  }
}
