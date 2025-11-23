package waterballsa.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import waterballsa.dto.UserInfo;
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
}
