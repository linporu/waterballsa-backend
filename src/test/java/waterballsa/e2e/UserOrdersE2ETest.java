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
 * E2E tests for user orders endpoint.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>GET /users/{userId}/orders - Get user's order list with pagination
 * </ul>
 */
@Sql(
    scripts = {"/test-data/cleanup.sql", "/test-data/orders.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/test-data/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserOrdersE2ETest extends BaseE2ETest {

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
  @DisplayName("Should successfully get user's order list")
  void shouldGetUserOrdersSuccessfully() {
    // Create two orders for testing
    Long order1Id = createOrder(1L);
    createOrder(2L);

    // Pay first order
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .post("/orders/{orderId}/action/pay", order1Id)
        .then()
        .statusCode(200);

    // Get user's orders
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/users/{userId}/orders", userId)
        .then()
        .statusCode(200)
        .body("orders", hasSize(2))
        .body("orders[0].id", notNullValue())
        .body("orders[0].orderNumber", notNullValue())
        .body("orders[0].status", is(oneOf("PAID", "UNPAID")))
        .body("orders[0].price", notNullValue())
        .body("orders[0].items", notNullValue())
        .body("orders[0].items", hasSize(greaterThan(0)))
        .body("orders[0].items[0].journeyId", notNullValue())
        .body("orders[0].items[0].journeyTitle", notNullValue())
        .body("orders[0].createdAt", notNullValue())
        .body("pagination.page", equalTo(1))
        .body("pagination.limit", equalTo(20))
        .body("pagination.total", equalTo(2));
  }

  @Test
  @DisplayName("Should get user orders with pagination")
  void shouldGetUserOrdersWithPagination() {
    // Create 3 orders
    createOrder(1L);
    createOrder(2L);
    createOrder(3L);

    // Get first page with limit 2
    given()
        .header("Authorization", bearerToken(userToken))
        .queryParam("page", 1)
        .queryParam("limit", 2)
        .when()
        .get("/users/{userId}/orders", userId)
        .then()
        .statusCode(200)
        .body("orders", hasSize(2))
        .body("pagination.page", equalTo(1))
        .body("pagination.limit", equalTo(2))
        .body("pagination.total", equalTo(3));

    // Get second page
    given()
        .header("Authorization", bearerToken(userToken))
        .queryParam("page", 2)
        .queryParam("limit", 2)
        .when()
        .get("/users/{userId}/orders", userId)
        .then()
        .statusCode(200)
        .body("orders", hasSize(1))
        .body("pagination.page", equalTo(2))
        .body("pagination.limit", equalTo(2))
        .body("pagination.total", equalTo(3));
  }

  @Test
  @DisplayName("Should get empty order list when user has no orders")
  void shouldGetEmptyOrderList() {
    given()
        .header("Authorization", bearerToken(userToken))
        .when()
        .get("/users/{userId}/orders", userId)
        .then()
        .statusCode(200)
        .body("orders", hasSize(0))
        .body("pagination.page", equalTo(1))
        .body("pagination.limit", equalTo(20))
        .body("pagination.total", equalTo(0));
  }

  @Test
  @DisplayName("Should sort orders by creation time descending (newest first)")
  void shouldSortOrdersByCreatedAtDesc() {
    // Create orders with small delays to ensure different timestamps
    Long firstOrderId = createOrder(1L);
    sleep(100);
    createOrder(2L);
    sleep(100);
    Long thirdOrderId = createOrder(3L);

    Response response =
        given()
            .header("Authorization", bearerToken(userToken))
            .when()
            .get("/users/{userId}/orders", userId)
            .then()
            .statusCode(200)
            .body("orders", hasSize(3))
            .extract()
            .response();

    // Verify orders are sorted by createdAt descending (newest first)
    Long firstOrderInResponse = response.jsonPath().getLong("orders[0].id");
    Long lastOrderInResponse = response.jsonPath().getLong("orders[2].id");

    // The newest order (thirdOrderId) should be first
    assertThat(firstOrderInResponse, equalTo(thirdOrderId));
    // The oldest order (firstOrderId) should be last
    assertThat(lastOrderInResponse, equalTo(firstOrderId));
  }

  @Test
  @DisplayName("Should fail when not authenticated")
  void shouldFailWhenNotAuthenticated() {
    given().when().get("/users/{userId}/orders", userId).then().statusCode(401);
  }

  @Test
  @DisplayName("Should fail when accessing other user's orders")
  void shouldFailWhenAccessingOtherUsersOrders() {
    // Create order for first user
    createOrder(1L);

    // Register and login as second user
    String secondUsername = "testuser2_" + System.currentTimeMillis();
    String secondPassword = "Test1234!";
    Long secondUserId = registerUser(secondUsername, secondPassword);
    String secondUserToken = loginAndGetToken(secondUsername, secondPassword);

    // Try to access first user's orders as second user
    given()
        .header("Authorization", bearerToken(secondUserToken))
        .when()
        .get("/users/{userId}/orders", userId)
        .then()
        .statusCode(404);

    // Verify second user can only access their own orders
    given()
        .header("Authorization", bearerToken(secondUserToken))
        .when()
        .get("/users/{userId}/orders", secondUserId)
        .then()
        .statusCode(200)
        .body("orders", hasSize(0));
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
   * Helper method to sleep for a specified time.
   *
   * @param millis milliseconds to sleep
   */
  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  // Helper method for assertThat
  private static <T> void assertThat(T actual, org.hamcrest.Matcher<? super T> matcher) {
    org.hamcrest.MatcherAssert.assertThat(actual, matcher);
  }
}
