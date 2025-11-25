package waterballsa.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import waterballsa.dto.OrderListResponse;
import waterballsa.dto.UserInfo;
import waterballsa.dto.UserJourneyListResponse;
import waterballsa.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public ResponseEntity<UserInfo> getCurrentUser() {
    logger.debug("Get current user profile request received");

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    Long userId = (Long) authentication.getPrincipal();
    logger.debug("Getting profile for user ID: {}", userId);

    UserInfo userInfo = userService.getUserById(userId);

    logger.info("Successfully retrieved profile for user ID: {}", userId);

    return ResponseEntity.ok(userInfo);
  }

  @GetMapping("/{userId}/orders")
  public ResponseEntity<OrderListResponse> getUserOrders(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int limit) {

    logger.debug("GET /users/{}/orders request received (page={}, limit={})", userId, page, limit);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Long authenticatedUserId = (Long) authentication.getPrincipal();

    OrderListResponse response =
        userService.getUserOrders(userId, authenticatedUserId, page, limit);

    logger.info("Successfully retrieved orders for user {}", userId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{userId}/journeys")
  public ResponseEntity<UserJourneyListResponse> getUserJourneys(@PathVariable Long userId) {
    logger.debug("GET /users/{}/journeys request received", userId);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Long authenticatedUserId = (Long) authentication.getPrincipal();

    UserJourneyListResponse response = userService.getUserJourneys(userId, authenticatedUserId);

    logger.info("Successfully retrieved journeys for user {}", userId);
    return ResponseEntity.ok(response);
  }
}
