package waterballsa.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import waterballsa.bdd.support.World;

/**
 * Step definitions for database operations in BDD tests.
 *
 * <p>This class provides generic, reusable step definitions for setting up test data directly in
 * the database. These steps are used in the Setup (Given) phase of tests.
 *
 * <p>Key principles:
 *
 * <ul>
 *   <li>Direct database manipulation: Faster than API calls for test setup
 *   <li>Test isolation: Each scenario starts with a clean slate
 *   <li>Atomic: Each step creates one type of entity
 *   <li>Reusable: Can be used across different feature files
 * </ul>
 *
 * <p>Design decisions:
 *
 * <ul>
 *   <li>Password handling: Accepts plain text passwords and automatically hashes them
 *   <li>Required vs optional fields: Uses getOrDefault() for optional fields with sensible defaults
 *   <li>Foreign keys: Assumes sequential IDs (user_id=1, journey_id=1) for simplicity in tests
 * </ul>
 */
public class DatabaseStepDefinitions {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private World world;

  /**
   * Create a test user directly in the database.
   *
   * <p>Usage example:
   *
   * <pre>
   * Given the database has a user:
   *   | username   | Alice     |
   *   | password   | Test1234! |
   *   | experience | 0         |
   * </pre>
   *
   * <p>The generated user ID is automatically stored in the variable "lastUserId" for use in
   * subsequent steps.
   *
   * @param dataTable DataTable containing user data with keys: username (required), password
   *     (required), experience (optional, default: 0)
   */
  @Given("the database has a user:")
  public void databaseHasUser(DataTable dataTable) {
    Map<String, String> userData = dataTable.asMap(String.class, String.class);

    String username = userData.get("username");
    String password = userData.get("password");
    int experience = Integer.parseInt(userData.getOrDefault("experience", "0"));

    // Hash the password using BCrypt
    String passwordHash = passwordEncoder.encode(password);

    KeyHolder keyHolder = new GeneratedKeyHolder();

    // Insert user with default role=STUDENT and level=1
    jdbcTemplate.update(
        connection -> {
          PreparedStatement ps =
              connection.prepareStatement(
                  "INSERT INTO users (username, password_hash, role, experience_points, level) "
                      + "VALUES (?, ?, 'STUDENT', ?, 1)",
                  Statement.RETURN_GENERATED_KEYS);
          ps.setString(1, username);
          ps.setString(2, passwordHash);
          ps.setInt(3, experience);
          return ps;
        },
        keyHolder);

    // Store the generated user ID for use in subsequent steps
    Map<String, Object> keys = keyHolder.getKeys();
    if (keys != null && !keys.isEmpty()) {
      Long userId = ((Number) keys.get("id")).longValue();
      world.setVariable("lastUserId", userId.toString());
    }
  }

  /**
   * Create a test journey directly in the database.
   *
   * <p>Usage example:
   *
   * <pre>
   * Given the database has a journey:
   *   | title       | Java 基礎課程           |
   *   | slug        | java-basics            |
   *   | description | 學習 Java 程式設計基礎  |
   *   | teacher     | 水球老師                |
   *   | price       | 1999.00                |
   * </pre>
   *
   * <p>The generated journey ID is automatically stored in the variable "lastJourneyId" for use in
   * subsequent steps.
   *
   * @param dataTable DataTable containing journey data with keys: title (required), slug
   *     (required), description (optional), teacher (required), price (required)
   */
  @Given("the database has a journey:")
  public void databaseHasJourney(DataTable dataTable) {
    Map<String, String> journeyData = dataTable.asMap(String.class, String.class);

    String title = journeyData.get("title");
    String slug = journeyData.get("slug");
    String description = journeyData.get("description");
    String teacher = journeyData.get("teacher");
    BigDecimal price = new BigDecimal(journeyData.get("price"));
    String coverImageUrl = journeyData.get("cover_image_url");

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(
        connection -> {
          PreparedStatement ps =
              connection.prepareStatement(
                  "INSERT INTO journeys (title, slug, description, teacher_name, price, cover_image_url, created_at, updated_at) "
                      + "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())",
                  Statement.RETURN_GENERATED_KEYS);
          ps.setString(1, title);
          ps.setString(2, slug);
          ps.setString(3, description);
          ps.setString(4, teacher);
          ps.setBigDecimal(5, price);
          ps.setString(6, coverImageUrl);
          return ps;
        },
        keyHolder);

    // Store the generated journey ID for use in subsequent steps
    Map<String, Object> keys = keyHolder.getKeys();
    if (keys != null && !keys.isEmpty()) {
      Long journeyId = ((Number) keys.get("id")).longValue();
      world.setVariable("lastJourneyId", journeyId.toString());
    }
  }

  /**
   * Create a test chapter directly in the database.
   *
   * <p>Usage example:
   *
   * <pre>
   * Given the database has a chapter:
   *   | journey_id  | {{lastJourneyId}} |
   *   | title       | 第一章             |
   *   | order_index | 1                 |
   * </pre>
   *
   * <p>The journey_id can be a literal value or a variable like {{lastJourneyId}}. The generated
   * chapter ID is automatically stored in the variable "lastChapterId" for use in subsequent steps.
   *
   * @param dataTable DataTable containing chapter data with keys: journey_id (required), title
   *     (required), order_index (required)
   */
  @Given("the database has a chapter:")
  public void databaseHasChapter(DataTable dataTable) {
    Map<String, String> chapterData = dataTable.asMap(String.class, String.class);

    // Support variable replacement for journey_id
    String journeyIdStr = world.replaceVariables(chapterData.get("journey_id"));
    long journeyId = Long.parseLong(journeyIdStr);

    String title = chapterData.get("title");
    int orderIndex = Integer.parseInt(chapterData.get("order_index"));

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(
        connection -> {
          PreparedStatement ps =
              connection.prepareStatement(
                  "INSERT INTO chapters (journey_id, title, order_index) VALUES (?, ?, ?)",
                  Statement.RETURN_GENERATED_KEYS);
          ps.setLong(1, journeyId);
          ps.setString(2, title);
          ps.setInt(3, orderIndex);
          return ps;
        },
        keyHolder);

    // Store the generated chapter ID for use in subsequent steps
    Map<String, Object> keys = keyHolder.getKeys();
    if (keys != null && !keys.isEmpty()) {
      Long chapterId = ((Number) keys.get("id")).longValue();
      world.setVariable("lastChapterId", chapterId.toString());
    }
  }

  /**
   * Create a test mission directly in the database.
   *
   * <p>Usage example:
   *
   * <pre>
   * Given the database has a mission:
   *   | chapter_id   | {{lastChapterId}} |
   *   | title        | 認識變數           |
   *   | type         | VIDEO             |
   *   | access_level | PURCHASED         |
   *   | order_index  | 1                 |
   * </pre>
   *
   * <p>The chapter_id can be a literal value or a variable like {{lastChapterId}}. The generated
   * mission ID is automatically stored in the variable "lastMissionId" for use in subsequent steps.
   *
   * @param dataTable DataTable containing mission data with keys: chapter_id (required), title
   *     (required), type (optional, default: VIDEO), access_level (optional, default: PURCHASED),
   *     order_index (required)
   */
  @Given("the database has a mission:")
  public void databaseHasMission(DataTable dataTable) {
    Map<String, String> missionData = dataTable.asMap(String.class, String.class);

    // Support variable replacement for chapter_id
    String chapterIdStr = world.replaceVariables(missionData.get("chapter_id"));
    long chapterId = Long.parseLong(chapterIdStr);

    String title = missionData.get("title");
    String type = missionData.getOrDefault("type", "VIDEO");
    String accessLevel = missionData.getOrDefault("access_level", "PURCHASED");
    int orderIndex = Integer.parseInt(missionData.get("order_index"));
    String description = missionData.get("description");

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(
        connection -> {
          PreparedStatement ps =
              connection.prepareStatement(
                  "INSERT INTO missions (chapter_id, title, type, access_level, order_index, description) "
                      + "VALUES (?, ?, ?::mission_type, ?::mission_access_level, ?, ?)",
                  Statement.RETURN_GENERATED_KEYS);
          ps.setLong(1, chapterId);
          ps.setString(2, title);
          ps.setString(3, type);
          ps.setString(4, accessLevel);
          ps.setInt(5, orderIndex);
          ps.setString(6, description);
          return ps;
        },
        keyHolder);

    // Store the generated mission ID for use in subsequent steps
    Map<String, Object> keys = keyHolder.getKeys();
    if (keys != null && !keys.isEmpty()) {
      Long missionId = ((Number) keys.get("id")).longValue();
      world.setVariable("lastMissionId", missionId.toString());
    }
  }

  /**
   * Create a test order directly in the database.
   *
   * <p>Usage example:
   *
   * <pre>
   * Given the database has an order:
   *   | user_id    | 1      |
   *   | journey_id | 1      |
   *   | status     | UNPAID |
   * </pre>
   *
   * @param dataTable DataTable containing order data with keys: user_id (required), journey_id
   *     (required), status (optional, default: UNPAID)
   */
  @Given("the database has an order:")
  public void databaseHasOrder(DataTable dataTable) {
    Map<String, String> orderData = dataTable.asMap(String.class, String.class);

    // Support variable replacement for user_id and journey_id
    String userIdStr = world.replaceVariables(orderData.get("user_id"));
    String journeyIdStr = world.replaceVariables(orderData.get("journey_id"));
    long userId = Long.parseLong(userIdStr);
    long journeyId = Long.parseLong(journeyIdStr);
    String status = orderData.getOrDefault("status", "UNPAID");

    // First, get the journey price
    BigDecimal journeyPrice =
        jdbcTemplate.queryForObject(
            "SELECT price FROM journeys WHERE id = ?", BigDecimal.class, journeyId);

    // Generate order number (simplified version for testing)
    String orderNumber = String.format("%d-%d-%d", System.currentTimeMillis(), userId, journeyId);

    // Insert order with appropriate timestamps based on status
    Long orderId;
    if ("PAID".equals(status)) {
      // For PAID orders: set paid_at to NOW(), no expired_at
      orderId =
          jdbcTemplate.queryForObject(
              "INSERT INTO orders (order_number, user_id, status, original_price, discount, price, "
                  + "created_at, paid_at, updated_at) "
                  + "VALUES (?, ?, ?::order_status, ?, 0, ?, NOW(), NOW(), NOW()) "
                  + "RETURNING id",
              Long.class,
              orderNumber,
              userId,
              status,
              journeyPrice,
              journeyPrice);
    } else {
      // For UNPAID/EXPIRED orders: set expired_at, no paid_at
      orderId =
          jdbcTemplate.queryForObject(
              "INSERT INTO orders (order_number, user_id, status, original_price, discount, price, "
                  + "created_at, expired_at, updated_at) "
                  + "VALUES (?, ?, ?::order_status, ?, 0, ?, NOW(), NOW() + INTERVAL '3 days', NOW()) "
                  + "RETURNING id",
              Long.class,
              orderNumber,
              userId,
              status,
              journeyPrice,
              journeyPrice);
    }

    // Insert order item
    jdbcTemplate.update(
        "INSERT INTO order_items (order_id, journey_id, quantity, original_price, discount, price) "
            + "VALUES (?, ?, 1, ?, 0, ?)",
        orderId,
        journeyId,
        journeyPrice,
        journeyPrice);

    // If order is PAID, also create user_journey record
    if ("PAID".equals(status)) {
      jdbcTemplate.update(
          "INSERT INTO user_journeys (user_id, journey_id, order_id, purchased_at) "
              + "VALUES (?, ?, ?, NOW())",
          userId,
          journeyId,
          orderId);
    }

    // Store the generated order ID for use in subsequent steps
    world.setVariable("lastOrderId", orderId.toString());
  }

  /**
   * Create a test reward directly in the database.
   *
   * <p>Usage example:
   *
   * <pre>
   * Given the database has a reward:
   *   | mission_id   | {{lastMissionId}} |
   *   | reward_type  | EXPERIENCE        |
   *   | reward_value | 100               |
   * </pre>
   *
   * <p>The mission_id can be a literal value or a variable like {{lastMissionId}}.
   *
   * @param dataTable DataTable containing reward data with keys: mission_id (required), reward_type
   *     (optional, default: EXPERIENCE), reward_value (optional, default: 100)
   */
  @Given("the database has a reward:")
  public void databaseHasReward(DataTable dataTable) {
    Map<String, String> rewardData = dataTable.asMap(String.class, String.class);

    // Support variable replacement for mission_id
    String missionIdStr = world.replaceVariables(rewardData.get("mission_id"));
    long missionId = Long.parseLong(missionIdStr);

    String rewardType = rewardData.getOrDefault("reward_type", "EXPERIENCE");
    int rewardValue = Integer.parseInt(rewardData.getOrDefault("reward_value", "100"));

    jdbcTemplate.update(
        "INSERT INTO rewards (mission_id, reward_type, reward_value) "
            + "VALUES (?, ?::reward_type, ?)",
        missionId,
        rewardType,
        rewardValue);
  }

  /**
   * Create a test user mission progress directly in the database.
   *
   * <p>Usage example:
   *
   * <pre>
   * Given the database has a user mission progress:
   *   | user_id              | 1           |
   *   | mission_id           | 1           |
   *   | status               | COMPLETED   |
   *   | watch_position_seconds | 100       |
   * </pre>
   *
   * @param dataTable DataTable containing progress data with keys: user_id (required), mission_id
   *     (required), status (optional, default: UNCOMPLETED), watch_position_seconds (optional,
   *     default: 0)
   */
  @Given("the database has a user mission progress:")
  public void databaseHasUserMissionProgress(DataTable dataTable) {
    Map<String, String> progressData = dataTable.asMap(String.class, String.class);

    long userId = Long.parseLong(progressData.get("user_id"));

    // Support variable replacement for mission_id
    String missionIdStr = world.replaceVariables(progressData.get("mission_id"));
    long missionId = Long.parseLong(missionIdStr);

    String status = progressData.getOrDefault("status", "UNCOMPLETED");
    int watchPosition = Integer.parseInt(progressData.getOrDefault("watch_position_seconds", "0"));

    jdbcTemplate.update(
        "INSERT INTO user_mission_progress (user_id, mission_id, status, watch_position_seconds) "
            + "VALUES (?, ?, ?::progress_status, ?)",
        userId,
        missionId,
        status,
        watchPosition);
  }

  /**
   * Create a test mission resource directly in the database.
   *
   * <p>Usage example:
   *
   * <pre>
   * Given the database has a mission resource:
   *   | mission_id       | {{lastMissionId}}           |
   *   | type             | VIDEO                       |
   *   | resource_url     | https://example.com/vid.mp4 |
   *   | content_order    | 0                           |
   *   | duration_seconds | 256                         |
   * </pre>
   *
   * <p>The mission_id can be a literal value or a variable like {{lastMissionId}}.
   *
   * @param dataTable DataTable containing resource data with keys: mission_id (required), type
   *     (optional, default: VIDEO), resource_url (optional), content_order (optional, default: 0),
   *     duration_seconds (optional)
   */
  @Given("the database has a mission resource:")
  public void databaseHasMissionResource(DataTable dataTable) {
    Map<String, String> resourceData = dataTable.asMap(String.class, String.class);

    // Support variable replacement for mission_id
    String missionIdStr = world.replaceVariables(resourceData.get("mission_id"));
    long missionId = Long.parseLong(missionIdStr);

    String resourceType = resourceData.getOrDefault("type", "VIDEO");
    String resourceUrl = resourceData.get("resource_url");
    int contentOrder = Integer.parseInt(resourceData.getOrDefault("content_order", "0"));
    Integer durationSeconds =
        resourceData.containsKey("duration_seconds")
            ? Integer.parseInt(resourceData.get("duration_seconds"))
            : null;

    jdbcTemplate.update(
        "INSERT INTO mission_resources (mission_id, resource_type, resource_url, content_order, duration_seconds) "
            + "VALUES (?, ?::resource_type, ?, ?, ?)",
        missionId,
        resourceType,
        resourceUrl,
        contentOrder,
        durationSeconds);
  }

  /**
   * Update the price of a journey in the database.
   *
   * <p>This is useful for testing price locking scenarios where we need to change the journey price
   * after an order has been created.
   *
   * <p>Usage example:
   *
   * <pre>
   * Given I update the journey with id "{{lastJourneyId}}" to price "2999.00"
   * </pre>
   *
   * @param journeyIdStr journey ID (supports variable replacement with {{variableName}})
   * @param newPrice new price value
   */
  @Given("I update the journey with id {string} to price {string}")
  public void updateJourneyPrice(String journeyIdStr, String newPrice) {
    // Support variable replacement for journey_id
    String processedJourneyId = world.replaceVariables(journeyIdStr);
    long journeyId = Long.parseLong(processedJourneyId);
    BigDecimal price = new BigDecimal(newPrice);

    jdbcTemplate.update(
        "UPDATE journeys SET price = ?, updated_at = NOW() WHERE id = ?", price, journeyId);
  }

  /**
   * Verify the count of unpaid orders for a specific user and journey in the database.
   *
   * <p>Usage example:
   *
   * <pre>
   * Then the database should have 1 unpaid order for user "{{lastUserId}}" and journey "{{lastJourneyId}}"
   * Then the database should have 2 unpaid orders for user "1"
   * </pre>
   *
   * @param expectedCount expected number of unpaid orders
   * @param userIdStr user ID (supports variable replacement)
   * @param journeyIdStr journey ID (supports variable replacement), optional - if not provided,
   *     counts all unpaid orders for the user
   */
  @Given("the database should have {int} unpaid order(s) for user {string} and journey {string}")
  public void verifyUnpaidOrderCountForUserAndJourney(
      int expectedCount, String userIdStr, String journeyIdStr) {
    String processedUserId = world.replaceVariables(userIdStr);
    String processedJourneyId = world.replaceVariables(journeyIdStr);
    long userId = Long.parseLong(processedUserId);
    long journeyId = Long.parseLong(processedJourneyId);

    // Count unpaid orders for this user and journey combination
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM orders o "
                + "INNER JOIN order_items oi ON o.id = oi.order_id "
                + "WHERE o.user_id = ? AND o.status = 'UNPAID' AND oi.journey_id = ?",
            Integer.class,
            userId,
            journeyId);

    if (count != expectedCount) {
      throw new AssertionError(
          String.format(
              "Expected %d unpaid orders for user %d and journey %d, but found %d",
              expectedCount, userId, journeyId, count));
    }
  }

  /**
   * Verify the total count of unpaid orders for a specific user in the database.
   *
   * <p>Usage example:
   *
   * <pre>
   * Then the database should have 2 unpaid orders for user "{{lastUserId}}"
   * </pre>
   *
   * @param expectedCount expected number of unpaid orders
   * @param userIdStr user ID (supports variable replacement)
   */
  @Given("the database should have {int} unpaid order(s) for user {string}")
  public void verifyUnpaidOrderCountForUser(int expectedCount, String userIdStr) {
    String processedUserId = world.replaceVariables(userIdStr);
    long userId = Long.parseLong(processedUserId);

    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM orders WHERE user_id = ? AND status = 'UNPAID'",
            Integer.class,
            userId);

    if (count != expectedCount) {
      throw new AssertionError(
          String.format(
              "Expected %d unpaid orders for user %d, but found %d", expectedCount, userId, count));
    }
  }
}
