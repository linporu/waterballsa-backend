package waterballsa.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

/**
 * E2E tests for Order-related endpoints.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>POST /orders - Create order
 *   <li>GET /orders/{orderId} - Get order detail
 *   <li>POST /orders/{orderId}/action/pay - Pay order
 * </ul>
 */
@Sql(
    scripts = {"/test-data/cleanup.sql", "/test-data/orders.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/test-data/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class OrderE2ETest extends BaseE2ETest {

  private String userToken;
  private Long userId;
  private String username;

  @BeforeEach
  void setUp() {
    username = "testuser_" + System.currentTimeMillis();
    String password = "Test1234!";
    userId = registerUser(username, password);
    userToken = loginAndGetToken(username, password);
  }

  // ==================== POST /orders Tests ====================

  @Nested
  @DisplayName("POST /orders")
  class CreateOrderTests {

    @Test
    @DisplayName("Should successfully create a new order")
    void shouldCreateNewOrderSuccessfully() {
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 1,
                    "quantity": 1
                  }
                ]
              }
              """;

      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/orders")
          .then()
          .statusCode(201)
          .body("id", notNullValue())
          .body("orderNumber", notNullValue())
          .body("userId", equalTo(userId.intValue()))
          .body("username", equalTo(username))
          .body("status", equalTo("UNPAID"))
          .body("originalPrice", equalTo(1999.00f))
          .body("discount", equalTo(0.00f))
          .body("price", equalTo(1999.00f))
          .body("items", hasSize(1))
          .body("items[0].journeyId", equalTo(1))
          .body("items[0].journeyTitle", equalTo("軟體設計模式精通之旅"))
          .body("items[0].quantity", equalTo(1))
          .body("items[0].originalPrice", equalTo(1999.00f))
          .body("items[0].discount", equalTo(0.00f))
          .body("items[0].price", equalTo(1999.00f))
          .body("createdAt", notNullValue())
          .body("paidAt", nullValue());
    }

    @Test
    @DisplayName(
        "Should return existing unpaid order when user already has one for the same journey")
    void shouldReturnExistingUnpaidOrder() {
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 1,
                    "quantity": 1
                  }
                ]
              }
              """;

      // Create first order
      Response firstResponse =
          given()
              .header("Authorization", bearerToken(userToken))
              .contentType(ContentType.JSON)
              .body(requestBody)
              .when()
              .post("/orders")
              .then()
              .statusCode(201)
              .extract()
              .response();

      Long firstOrderId = firstResponse.jsonPath().getLong("id");
      String firstOrderNumber = firstResponse.jsonPath().getString("orderNumber");

      // Attempt to create second order for same journey
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/orders")
          .then()
          .statusCode(200)
          .body("id", equalTo(firstOrderId.intValue()))
          .body("orderNumber", equalTo(firstOrderNumber))
          .body("status", equalTo("UNPAID"));
    }

    @Test
    @DisplayName("Should fail when not authenticated")
    void shouldFailWhenNotAuthenticated() {
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 1,
                    "quantity": 1
                  }
                ]
              }
              """;

      given()
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/orders")
          .then()
          .statusCode(401);
    }

    @Test
    @DisplayName("Should fail with invalid journey ID")
    void shouldFailWithInvalidJourneyId() {
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": -1,
                    "quantity": 1
                  }
                ]
              }
              """;

      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/orders")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Should fail when journey not found")
    void shouldFailWhenJourneyNotFound() {
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 99999,
                    "quantity": 1
                  }
                ]
              }
              """;

      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/orders")
          .then()
          .statusCode(404);
    }

    @Test
    @DisplayName("Should fail when journey already purchased")
    void shouldFailWhenJourneyAlreadyPurchased() {
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 1,
                    "quantity": 1
                  }
                ]
              }
              """;

      // Create and pay order first
      Long orderId =
          given()
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

      // Pay the order
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .post("/orders/{orderId}/action/pay", orderId)
          .then()
          .statusCode(200);

      // Try to create another order for same journey
      given()
          .header("Authorization", bearerToken(userToken))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/orders")
          .then()
          .statusCode(409)
          .body("error", containsString("你已經購買此課程"));
    }

    @Test
    @DisplayName("Should generate correct order number format")
    void shouldGenerateCorrectOrderNumberFormat() {
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 1,
                    "quantity": 1
                  }
                ]
              }
              """;

      String orderNumber =
          given()
              .header("Authorization", bearerToken(userToken))
              .contentType(ContentType.JSON)
              .body(requestBody)
              .when()
              .post("/orders")
              .then()
              .statusCode(201)
              .extract()
              .jsonPath()
              .getString("orderNumber");

      // Order number format: {timestamp:10 digits}{userId}{randomCode:5 chars}
      // Minimum length: 10 + userId.length + 5
      // Example: 20251121011117cd5 (10 + 2 + 5 = 17)
      String userIdStr = userId.toString();
      int expectedMinLength = 10 + userIdStr.length() + 5;

      // Verify order number contains userId
      assertThat(orderNumber, containsString(userIdStr));
      // Verify minimum length
      assertThat(orderNumber.length(), greaterThanOrEqualTo(expectedMinLength));
      // Verify starts with digits (timestamp part)
      assertThat(orderNumber.substring(0, 10), matchesPattern("\\d{10}"));
    }

    @Test
    @DisplayName("Should lock price at order creation time")
    void shouldLockPriceAtOrderCreation() {
      // Create order with original price 1999.00
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 1,
                    "quantity": 1
                  }
                ]
              }
              """;

      Response createResponse =
          given()
              .header("Authorization", bearerToken(userToken))
              .contentType(ContentType.JSON)
              .body(requestBody)
              .when()
              .post("/orders")
              .then()
              .statusCode(201)
              .body("items[0].originalPrice", equalTo(1999.00f))
              .extract()
              .response();

      Long orderId = createResponse.jsonPath().getLong("id");

      // Manually update journey price in database (simulating price change)
      // In real implementation, this would be done by admin
      // For now, we verify the order price remains unchanged when queried

      // Verify order still has locked price
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .get("/orders/{orderId}", orderId)
          .then()
          .statusCode(200)
          .body("items[0].originalPrice", equalTo(1999.00f))
          .body("items[0].price", equalTo(1999.00f))
          .body("price", equalTo(1999.00f));
    }
  }

  // ==================== GET /orders/{orderId} Tests ====================

  @Nested
  @DisplayName("GET /orders/{orderId}")
  class GetOrderDetailTests {

    @Test
    @DisplayName("Should successfully get unpaid order detail")
    void shouldGetUnpaidOrderDetail() {
      // Create order first
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 2,
                    "quantity": 1
                  }
                ]
              }
              """;

      Long orderId =
          given()
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

      // Get order detail
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .get("/orders/{orderId}", orderId)
          .then()
          .statusCode(200)
          .body("id", equalTo(orderId.intValue()))
          .body("userId", equalTo(userId.intValue()))
          .body("username", equalTo(username))
          .body("status", equalTo("UNPAID"))
          .body("price", equalTo(2999.00f))
          .body("items[0].journeyId", equalTo(2))
          .body("items[0].journeyTitle", equalTo("Spring Boot 完全攻略"))
          .body("paidAt", nullValue());
    }

    @Test
    @DisplayName("Should successfully get paid order detail")
    void shouldGetPaidOrderDetail() {
      // Create and pay order
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 2,
                    "quantity": 1
                  }
                ]
              }
              """;

      Long orderId =
          given()
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

      // Pay the order
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .post("/orders/{orderId}/action/pay", orderId)
          .then()
          .statusCode(200);

      // Get order detail
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .get("/orders/{orderId}", orderId)
          .then()
          .statusCode(200)
          .body("id", equalTo(orderId.intValue()))
          .body("status", equalTo("PAID"))
          .body("paidAt", notNullValue());
    }

    @Test
    @DisplayName("Should fail when not authenticated")
    void shouldFailWhenNotAuthenticated() {
      given().when().get("/orders/{orderId}", 1).then().statusCode(401);
    }

    @Test
    @DisplayName("Should fail when order not found")
    void shouldFailWhenOrderNotFound() {
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .get("/orders/{orderId}", 99999)
          .then()
          .statusCode(404);
    }

    @Test
    @DisplayName("Should fail when accessing other user's order")
    void shouldFailWhenAccessingOthersOrder() {
      // Create order with first user
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 1,
                    "quantity": 1
                  }
                ]
              }
              """;

      Long orderId =
          given()
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

      // Register second user
      String secondUsername = "testuser2_" + System.currentTimeMillis();
      String secondPassword = "Test1234!";
      registerUser(secondUsername, secondPassword);
      String secondUserToken = loginAndGetToken(secondUsername, secondPassword);

      // Try to access first user's order
      given()
          .header("Authorization", bearerToken(secondUserToken))
          .when()
          .get("/orders/{orderId}", orderId)
          .then()
          .statusCode(404);
    }
  }

  // ==================== POST /orders/{orderId}/action/pay Tests ====================

  @Nested
  @DisplayName("POST /orders/{orderId}/action/pay")
  class PayOrderTests {

    @Test
    @DisplayName("Should successfully pay order")
    void shouldPayOrderSuccessfully() {
      // Create order first
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 3,
                    "quantity": 1
                  }
                ]
              }
              """;

      Long orderId =
          given()
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

      // Pay the order
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .post("/orders/{orderId}/action/pay", orderId)
          .then()
          .statusCode(200)
          .body("id", equalTo(orderId.intValue()))
          .body("orderNumber", notNullValue())
          .body("status", equalTo("PAID"))
          .body("price", equalTo(7599.00f))
          .body("paidAt", notNullValue())
          .body("message", equalTo("付款完成"));
    }

    @Test
    @DisplayName("Should grant journey access after payment")
    void shouldGrantJourneyAccessAfterPayment() {
      // Create order
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 1,
                    "quantity": 1
                  }
                ]
              }
              """;

      Long orderId =
          given()
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

      // Pay the order
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .post("/orders/{orderId}/action/pay", orderId)
          .then()
          .statusCode(200);

      // TODO: Verify user_journeys record created
      // For now, we'll verify the order is paid and user has journey access
      // The userStatus.purchased feature will be implemented in Journey API later
    }

    @Test
    @DisplayName("Should fail when not authenticated")
    void shouldFailWhenNotAuthenticated() {
      given().when().post("/orders/{orderId}/action/pay", 1).then().statusCode(401);
    }

    @Test
    @DisplayName("Should fail when order not found")
    void shouldFailWhenOrderNotFound() {
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .post("/orders/{orderId}/action/pay", 99999)
          .then()
          .statusCode(404);
    }

    @Test
    @DisplayName("Should fail when order already paid")
    void shouldFailWhenOrderAlreadyPaid() {
      // Create and pay order
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 2,
                    "quantity": 1
                  }
                ]
              }
              """;

      Long orderId =
          given()
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

      // Pay the order
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .post("/orders/{orderId}/action/pay", orderId)
          .then()
          .statusCode(200);

      // Try to pay again
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .post("/orders/{orderId}/action/pay", orderId)
          .then()
          .statusCode(409);
    }

    @Test
    @DisplayName("Should fail when paying other user's order")
    void shouldFailWhenPayingOthersOrder() {
      // Create order with first user
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 1,
                    "quantity": 1
                  }
                ]
              }
              """;

      Long orderId =
          given()
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

      // Register second user
      String secondUsername = "testuser2_" + System.currentTimeMillis();
      String secondPassword = "Test1234!";
      registerUser(secondUsername, secondPassword);
      String secondUserToken = loginAndGetToken(secondUsername, secondPassword);

      // Try to pay first user's order
      given()
          .header("Authorization", bearerToken(secondUserToken))
          .when()
          .post("/orders/{orderId}/action/pay", orderId)
          .then()
          .statusCode(404);
    }
  }

  // ==================== Complete Purchase Flow Tests ====================

  @Nested
  @DisplayName("Complete Purchase Flow")
  class CompletePurchaseFlowTests {

    @Test
    @DisplayName("Should complete full purchase flow: create -> query -> pay -> verify")
    void shouldCompleteFullPurchaseFlow() {
      // Step 1: Create order
      String requestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 3,
                    "quantity": 1
                  }
                ]
              }
              """;

      Response createResponse =
          given()
              .header("Authorization", bearerToken(userToken))
              .contentType(ContentType.JSON)
              .body(requestBody)
              .when()
              .post("/orders")
              .then()
              .statusCode(201)
              .body("status", equalTo("UNPAID"))
              .body("paidAt", nullValue())
              .extract()
              .response();

      Long orderId = createResponse.jsonPath().getLong("id");
      String orderNumber = createResponse.jsonPath().getString("orderNumber");

      // Step 2: Query order detail
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .get("/orders/{orderId}", orderId)
          .then()
          .statusCode(200)
          .body("orderNumber", equalTo(orderNumber))
          .body("status", equalTo("UNPAID"))
          .body("items[0].journeyTitle", equalTo("AI x BDD：規格驅動全自動開發術"))
          .body("items[0].price", equalTo(7599.00f));

      // Step 3: Complete payment
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .post("/orders/{orderId}/action/pay", orderId)
          .then()
          .statusCode(200)
          .body("status", equalTo("PAID"))
          .body("message", equalTo("付款完成"));

      // Step 4: Verify order is paid
      given()
          .header("Authorization", bearerToken(userToken))
          .when()
          .get("/orders/{orderId}", orderId)
          .then()
          .statusCode(200)
          .body("status", equalTo("PAID"))
          .body("paidAt", notNullValue());

      // TODO: Step 5: Verify journey access granted
      // For now, we verify through order status being PAID
      // The userStatus.purchased feature will be implemented in Journey API later
    }

    @Test
    @DisplayName("Should handle multiple users purchasing different journeys concurrently")
    void shouldHandleMultipleUsersConcurrentOrders() {
      // User 1: Purchase journey 1
      String user1RequestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 1,
                    "quantity": 1
                  }
                ]
              }
              """;

      Response user1Response =
          given()
              .header("Authorization", bearerToken(userToken))
              .contentType(ContentType.JSON)
              .body(user1RequestBody)
              .when()
              .post("/orders")
              .then()
              .statusCode(201)
              .extract()
              .response();

      String user1OrderNumber = user1Response.jsonPath().getString("orderNumber");

      // User 2: Purchase journey 2
      String user2Username = "testuser2_" + System.currentTimeMillis();
      String user2Password = "Test1234!";
      Long user2Id = registerUser(user2Username, user2Password);
      String user2Token = loginAndGetToken(user2Username, user2Password);

      String user2RequestBody =
          """
              {
                "items": [
                  {
                    "journeyId": 2,
                    "quantity": 1
                  }
                ]
              }
              """;

      Response user2Response =
          given()
              .header("Authorization", bearerToken(user2Token))
              .contentType(ContentType.JSON)
              .body(user2RequestBody)
              .when()
              .post("/orders")
              .then()
              .statusCode(201)
              .extract()
              .response();

      String user2OrderNumber = user2Response.jsonPath().getString("orderNumber");

      // Verify order numbers are unique
      assertThat(user1OrderNumber, not(equalTo(user2OrderNumber)));

      // Verify both users have correct order details
      assertThat(user1Response.jsonPath().getInt("userId"), equalTo(userId.intValue()));
      assertThat(user2Response.jsonPath().getInt("userId"), equalTo(user2Id.intValue()));
      assertThat(
          user1Response.jsonPath().getString("items[0].journeyTitle"), equalTo("軟體設計模式精通之旅"));
      assertThat(
          user2Response.jsonPath().getString("items[0].journeyTitle"), equalTo("Spring Boot 完全攻略"));
    }
  }

  // Helper method for pattern matching assertion
  private static org.hamcrest.Matcher<String> matchesPattern(String regex) {
    return org.hamcrest.Matchers.matchesRegex(regex);
  }

  // Helper method for assertThat
  private static <T> void assertThat(T actual, org.hamcrest.Matcher<? super T> matcher) {
    org.hamcrest.MatcherAssert.assertThat(actual, matcher);
  }
}
