package waterballsa.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import waterballsa.entity.MissionEntity;
import waterballsa.entity.MissionTypeEntity;
import waterballsa.entity.ProgressStatusEntity;
import waterballsa.entity.UserMissionProgressEntity;
import waterballsa.exception.InvalidWatchPositionException;
import waterballsa.exception.MissionAlreadyDeliveredException;
import waterballsa.exception.MissionNotCompletedException;
import waterballsa.exception.MissionNotFoundException;
import waterballsa.exception.ProgressAccessDeniedException;
import waterballsa.exception.UnsupportedMissionTypeException;
import waterballsa.repository.MissionRepository;

/**
 * Validator for user mission progress operations.
 *
 * <p>This validator handles all validation logic related to user mission progress, including access
 * control, mission type validation, watch position validation, and delivery validation.
 */
@Component
public class ProgressValidator {

  private static final Logger logger = LoggerFactory.getLogger(ProgressValidator.class);

  private final AuthValidator authValidator;
  private final MissionRepository missionRepository;

  public ProgressValidator(AuthValidator authValidator, MissionRepository missionRepository) {
    this.authValidator = authValidator;
    this.missionRepository = missionRepository;
  }

  /**
   * Validates that the user has access to the progress data.
   *
   * <p>This ensures the user can only access their own progress.
   *
   * @param pathUserId the user ID from the path parameter
   * @param currentUserId the authenticated user ID
   * @throws ProgressAccessDeniedException if user attempts to access another user's progress
   */
  public void validateProgressAccess(Long pathUserId, Long currentUserId) {
    authValidator.validateUserAuthenticated(currentUserId);

    if (!pathUserId.equals(currentUserId)) {
      logger.warn("User {} attempted to access progress of user {}", currentUserId, pathUserId);
      throw new ProgressAccessDeniedException("Cannot access progress for other users");
    }
  }

  /**
   * Validates that the mission exists.
   *
   * @param missionId the mission ID
   * @throws MissionNotFoundException if mission not found
   */
  public void validateMissionExists(Long missionId) {
    missionRepository
        .findByIdAndDeletedAtIsNull(missionId)
        .orElseThrow(() -> new MissionNotFoundException(missionId));
  }

  /**
   * Validates that the mission exists and returns it.
   *
   * <p>This method combines validation and retrieval to avoid duplicate database queries.
   *
   * @param missionId the mission ID
   * @return the validated Mission entity
   * @throws MissionNotFoundException if mission not found
   */
  public MissionEntity validateAndGetMission(Long missionId) {
    return missionRepository
        .findByIdWithDetails(missionId)
        .orElseThrow(() -> new MissionNotFoundException(missionId));
  }

  /**
   * Validates that the mission type supports progress tracking.
   *
   * <p>Currently, only VIDEO missions support progress tracking.
   *
   * @param mission the mission to validate
   * @throws UnsupportedMissionTypeException if mission type does not support progress tracking
   */
  public void validateMissionTypeSupportsProgress(MissionEntity mission) {
    if (mission.getType() != MissionTypeEntity.VIDEO) {
      throw new UnsupportedMissionTypeException("Mission type does not support progress tracking");
    }
  }

  /**
   * Validates that the watch position is valid.
   *
   * @param watchPositionSeconds the watch position in seconds
   * @throws InvalidWatchPositionException if watch position is null or negative
   */
  public void validateWatchPosition(Integer watchPositionSeconds) {
    if (watchPositionSeconds == null || watchPositionSeconds < 0) {
      throw new InvalidWatchPositionException("Watch position cannot be negative");
    }
  }

  /**
   * Validates that the mission has not already been delivered.
   *
   * @param progress the user mission progress
   * @throws MissionAlreadyDeliveredException if mission already delivered
   */
  public void validateNotAlreadyDelivered(UserMissionProgressEntity progress) {
    if (progress != null && progress.getStatus() == ProgressStatusEntity.DELIVERED) {
      throw new MissionAlreadyDeliveredException("Mission has already been delivered");
    }
  }

  /**
   * Validates that video mission is completed before delivery.
   *
   * <p>For VIDEO missions, the progress status must be COMPLETED before delivery. Non-video
   * missions can be delivered immediately.
   *
   * @param mission the mission
   * @param progress the user mission progress
   * @throws MissionNotCompletedException if video mission not completed
   */
  public void validateVideoMissionCompleted(
      MissionEntity mission, UserMissionProgressEntity progress) {
    boolean isVideoMission = mission.getType() == MissionTypeEntity.VIDEO;
    boolean isNotCompleted =
        progress == null || progress.getStatus() != ProgressStatusEntity.COMPLETED;

    if (isVideoMission && isNotCompleted) {
      throw new MissionNotCompletedException("Video mission must be COMPLETED before delivery");
    }
  }
}
