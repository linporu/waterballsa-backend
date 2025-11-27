package waterballsa.service;

import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import waterballsa.dto.DeliverResponse;
import waterballsa.dto.UserMissionProgressResponse;
import waterballsa.entity.Mission;
import waterballsa.entity.MissionResource;
import waterballsa.entity.ProgressStatus;
import waterballsa.entity.User;
import waterballsa.entity.UserMissionProgress;
import waterballsa.exception.UnauthorizedException;
import waterballsa.repository.UserMissionProgressRepository;
import waterballsa.repository.UserRepository;
import waterballsa.validator.ProgressValidator;

@Service
public class ProgressService {

  private static final Logger logger = LoggerFactory.getLogger(ProgressService.class);
  private static final Integer DEFAULT_EXPERIENCE_REWARD = 100;

  private final ProgressValidator progressValidator;
  private final UserMissionProgressRepository progressRepository;
  private final UserRepository userRepository;

  public ProgressService(
      ProgressValidator progressValidator,
      UserMissionProgressRepository progressRepository,
      UserRepository userRepository) {
    this.progressValidator = progressValidator;
    this.progressRepository = progressRepository;
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

    progressValidator.validateProgressAccess(pathUserId, currentUserId);
    progressValidator.validateMissionExists(missionId);

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
      @NonNull Long pathUserId, Long missionId, Integer watchPositionSeconds, Long currentUserId) {
    logger.debug(
        "Updating progress for user: {}, mission: {}, watchPosition: {}, currentUser: {}",
        pathUserId,
        missionId,
        watchPositionSeconds,
        currentUserId);

    progressValidator.validateProgressAccess(pathUserId, currentUserId);
    progressValidator.validateWatchPosition(watchPositionSeconds);

    Mission mission = progressValidator.validateAndGetMission(missionId);
    progressValidator.validateMissionTypeSupportsProgress(mission);

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

  /**
   * Deliver a mission to receive experience points.
   *
   * @param pathUserId User ID from path parameter
   * @param missionId Mission ID
   * @param currentUserId Current authenticated user ID
   * @return DeliverResponse with experience gained and user stats
   */
  @Transactional
  public DeliverResponse deliverMission(
      @NonNull Long pathUserId, Long missionId, Long currentUserId) {
    logger.debug(
        "Delivering mission for user: {}, mission: {}, currentUser: {}",
        pathUserId,
        missionId,
        currentUserId);

    progressValidator.validateProgressAccess(pathUserId, currentUserId);

    Mission mission = progressValidator.validateAndGetMission(missionId);
    User user = findUserOrThrow(pathUserId);
    UserMissionProgress progress = findProgress(pathUserId, missionId);

    progressValidator.validateNotAlreadyDelivered(progress);
    progressValidator.validateVideoMissionCompleted(mission, progress);

    progress = getOrCreateProgress(user, mission, progress);
    progress.markAsDelivered();
    progressRepository.save(progress);

    Integer experienceGained = grantExperienceReward(user);

    logger.info(
        "Successfully delivered mission for user: {}, mission: {}, XP gained: {}",
        pathUserId,
        missionId,
        experienceGained);

    return new DeliverResponse(
        "任務交付成功", experienceGained, user.getExperiencePoints(), user.getLevel());
  }

  // ==================== Helper Methods ====================

  private Integer getVideoDuration(Mission mission) {
    return mission.getResources().stream()
        .filter(resource -> !resource.isDeleted())
        .sorted(Comparator.comparing(MissionResource::getContentOrder))
        .filter(resource -> resource.getDurationSeconds() != null)
        .map(MissionResource::getDurationSeconds)
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

  private User findUserOrThrow(@NonNull Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new UnauthorizedException("User not found: " + userId));
  }

  private UserMissionProgress findProgress(Long userId, Long missionId) {
    return progressRepository
        .findByUserIdAndMissionIdAndDeletedAtIsNull(userId, missionId)
        .orElse(null);
  }

  private UserMissionProgress getOrCreateProgress(
      User user, Mission mission, UserMissionProgress progress) {
    if (progress == null) {
      return new UserMissionProgress(user, mission);
    }
    return progress;
  }

  private Integer grantExperienceReward(User user) {
    Integer experienceGained = DEFAULT_EXPERIENCE_REWARD;
    user.addExperience(experienceGained);
    userRepository.save(user);
    return experienceGained;
  }
}
