package waterballsa.service;

import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import waterballsa.dto.UserMissionProgressResponse;
import waterballsa.entity.Mission;
import waterballsa.entity.MissionContent;
import waterballsa.entity.MissionType;
import waterballsa.entity.ProgressStatus;
import waterballsa.entity.User;
import waterballsa.entity.UserMissionProgress;
import waterballsa.exception.InvalidWatchPositionException;
import waterballsa.exception.MissionNotFoundException;
import waterballsa.exception.ProgressAccessDeniedException;
import waterballsa.exception.UnauthorizedException;
import waterballsa.exception.UnsupportedMissionTypeException;
import waterballsa.repository.MissionRepository;
import waterballsa.repository.UserMissionProgressRepository;
import waterballsa.repository.UserRepository;
import waterballsa.util.AuthenticationValidator;

@Service
public class ProgressService {

  private static final Logger logger = LoggerFactory.getLogger(ProgressService.class);

  private final UserMissionProgressRepository progressRepository;
  private final MissionRepository missionRepository;
  private final UserRepository userRepository;

  public ProgressService(
      UserMissionProgressRepository progressRepository,
      MissionRepository missionRepository,
      UserRepository userRepository) {
    this.progressRepository = progressRepository;
    this.missionRepository = missionRepository;
    this.userRepository = userRepository;
  }

  /**
   * Get user's progress for a specific mission.
   *
   * @param pathUserId User ID from path parameter
   * @param missionId Mission ID
   * @param currentUserId Current authenticated user ID
   * @return UserMissionProgressResponse
   */
  @Transactional(readOnly = true)
  public UserMissionProgressResponse getProgress(
      Long pathUserId, Long missionId, Long currentUserId) {
    logger.debug(
        "Getting progress for user: {}, mission: {}, currentUser: {}",
        pathUserId,
        missionId,
        currentUserId);

    validateAuthentication(currentUserId);
    validateUserAccess(pathUserId, currentUserId);
    validateMissionExists(missionId);

    UserMissionProgress progress =
        progressRepository
            .findByUserIdAndMissionIdAndDeletedAtIsNull(pathUserId, missionId)
            .orElse(null);

    if (progress == null) {
      logger.debug("No progress record found, returning default progress");
      return new UserMissionProgressResponse(missionId, ProgressStatus.UNCOMPLETED.name(), 0);
    }

    logger.info("Successfully retrieved progress for user: {}, mission: {}", pathUserId, missionId);
    return mapToResponse(progress);
  }

  /**
   * Update user's progress for a specific mission (upsert operation).
   *
   * @param pathUserId User ID from path parameter
   * @param missionId Mission ID
   * @param watchPositionSeconds Watch position in seconds
   * @param currentUserId Current authenticated user ID
   * @return UserMissionProgressResponse
   */
  @Transactional
  public UserMissionProgressResponse updateProgress(
      Long pathUserId, Long missionId, Integer watchPositionSeconds, Long currentUserId) {
    logger.debug(
        "Updating progress for user: {}, mission: {}, watchPosition: {}, currentUser: {}",
        pathUserId,
        missionId,
        watchPositionSeconds,
        currentUserId);

    validateAuthentication(currentUserId);
    validateUserAccess(pathUserId, currentUserId);
    validateWatchPosition(watchPositionSeconds);

    Mission mission = validateMissionExistsAndReturn(missionId);
    validateMissionTypeSupportsProgress(mission);

    Integer videoDuration = getVideoDuration(mission);
    Integer cappedPosition = capWatchPosition(watchPositionSeconds, videoDuration);

    UserMissionProgress progress =
        progressRepository
            .findByUserIdAndMissionIdAndDeletedAtIsNull(pathUserId, missionId)
            .orElseGet(
                () -> {
                  User user =
                      userRepository
                          .findById(pathUserId)
                          .orElseThrow(
                              () -> new UnauthorizedException("User not found: " + pathUserId));
                  return new UserMissionProgress(user, mission);
                });

    progress.updateWatchPosition(cappedPosition);

    // Check if video is completed (reached duration)
    if (videoDuration != null && cappedPosition.equals(videoDuration)) {
      progress.markAsCompleted();
    }

    progressRepository.save(progress);

    logger.info(
        "Successfully updated progress for user: {}, mission: {}, position: {}, status: {}",
        pathUserId,
        missionId,
        cappedPosition,
        progress.getStatus());

    return mapToResponse(progress);
  }

  private void validateAuthentication(Long currentUserId) {
    AuthenticationValidator.validateUserAuthenticated(currentUserId);
  }

  private void validateUserAccess(Long pathUserId, Long currentUserId) {
    if (!pathUserId.equals(currentUserId)) {
      logger.warn("User {} attempted to access progress of user {}", currentUserId, pathUserId);
      throw new ProgressAccessDeniedException("Cannot access progress for other users");
    }
  }

  private void validateMissionExists(Long missionId) {
    missionRepository
        .findByIdAndDeletedAtIsNull(missionId)
        .orElseThrow(() -> new MissionNotFoundException(missionId));
  }

  private Mission validateMissionExistsAndReturn(Long missionId) {
    return missionRepository
        .findByIdWithDetails(missionId)
        .orElseThrow(() -> new MissionNotFoundException(missionId));
  }

  private void validateMissionTypeSupportsProgress(Mission mission) {
    if (mission.getType() != MissionType.VIDEO) {
      logger.warn(
          "Attempted to update progress for non-VIDEO mission: {}, type: {}",
          mission.getId(),
          mission.getType());
      throw new UnsupportedMissionTypeException("Mission type does not support progress tracking");
    }
  }

  private void validateWatchPosition(Integer watchPositionSeconds) {
    if (watchPositionSeconds == null || watchPositionSeconds < 0) {
      throw new InvalidWatchPositionException("Watch position cannot be negative");
    }
  }

  private Integer getVideoDuration(Mission mission) {
    return mission.getContents().stream()
        .filter(content -> !content.isDeleted())
        .sorted(Comparator.comparing(MissionContent::getContentOrder))
        .filter(content -> content.getDurationSeconds() != null)
        .map(MissionContent::getDurationSeconds)
        .findFirst()
        .orElse(null);
  }

  private Integer capWatchPosition(Integer watchPosition, Integer duration) {
    if (duration == null) {
      return watchPosition;
    }
    return Math.min(watchPosition, duration);
  }

  private UserMissionProgressResponse mapToResponse(UserMissionProgress progress) {
    return new UserMissionProgressResponse(
        progress.getMission().getId(),
        progress.getStatus().name(),
        progress.getWatchPositionSeconds());
  }
}
