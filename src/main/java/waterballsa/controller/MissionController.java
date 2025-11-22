package waterballsa.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import waterballsa.dto.MissionDetailResponse;
import waterballsa.service.MissionService;

@RestController
@RequestMapping("/journeys")
public class MissionController {

  private static final Logger logger = LoggerFactory.getLogger(MissionController.class);

  private final MissionService missionService;

  public MissionController(MissionService missionService) {
    this.missionService = missionService;
  }

  /**
   * Get mission details.
   *
   * @param journeyId Journey ID
   * @param missionId Mission ID
   * @return Mission details response
   */
  @GetMapping("/{journeyId}/missions/{missionId}")
  public ResponseEntity<MissionDetailResponse> getMissionDetail(
      @PathVariable Long journeyId, @PathVariable Long missionId) {
    logger.debug("GET /journeys/{}/missions/{} request received", journeyId, missionId);

    // Get current user ID if authenticated
    Long userId = getCurrentUserId();

    MissionDetailResponse response = missionService.getMissionDetail(journeyId, missionId, userId);

    logger.info(
        "Successfully returned mission details for missionId: {}, userId: {}", missionId, userId);

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
