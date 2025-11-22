package waterballsa.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import waterballsa.dto.UpdateProgressRequest;
import waterballsa.dto.UserMissionProgressResponse;
import waterballsa.service.ProgressService;

@RestController
@RequestMapping("/users")
public class ProgressController {

  private static final Logger logger = LoggerFactory.getLogger(ProgressController.class);

  private final ProgressService progressService;

  public ProgressController(ProgressService progressService) {
    this.progressService = progressService;
  }

  /**
   * Get user's mission progress.
   *
   * @param userId User ID from path
   * @param missionId Mission ID
   * @return User's progress for the mission
   */
  @GetMapping("/{userId}/missions/{missionId}/progress")
  public ResponseEntity<UserMissionProgressResponse> getProgress(
      @PathVariable Long userId, @PathVariable Long missionId) {
    logger.debug("GET /users/{}/missions/{}/progress request received", userId, missionId);

    Long currentUserId = getCurrentUserId();

    UserMissionProgressResponse response =
        progressService.getProgress(userId, missionId, currentUserId);

    logger.info("Successfully returned progress for userId: {}, missionId: {}", userId, missionId);

    return ResponseEntity.ok(response);
  }

  /**
   * Update user's mission progress (upsert).
   *
   * @param userId User ID from path
   * @param missionId Mission ID
   * @param request Update progress request with watchPositionSeconds
   * @return Updated progress
   */
  @PutMapping("/{userId}/missions/{missionId}/progress")
  public ResponseEntity<UserMissionProgressResponse> updateProgress(
      @PathVariable @NonNull Long userId,
      @PathVariable Long missionId,
      @RequestBody UpdateProgressRequest request) {
    logger.debug(
        "PUT /users/{}/missions/{}/progress request received with watchPosition: {}",
        userId,
        missionId,
        request.watchPositionSeconds());

    Long currentUserId = getCurrentUserId();

    UserMissionProgressResponse response =
        progressService.updateProgress(
            userId, missionId, request.watchPositionSeconds(), currentUserId);

    logger.info(
        "Successfully updated progress for userId: {}, missionId: {}, status: {}",
        userId,
        missionId,
        response.status());

    return ResponseEntity.ok(response);
  }

  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof Long) {
      return (Long) principal;
    }

    return null;
  }
}
