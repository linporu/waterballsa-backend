package waterballsa.service;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import waterballsa.dto.MissionContentDTO;
import waterballsa.dto.MissionDetailResponse;
import waterballsa.dto.MissionRewardDTO;
import waterballsa.entity.Mission;
import waterballsa.entity.MissionAccessLevel;
import waterballsa.entity.MissionContent;
import waterballsa.exception.ForbiddenException;
import waterballsa.exception.MissionNotFoundException;
import waterballsa.repository.MissionRepository;
import waterballsa.repository.UserJourneyPurchaseRepository;

@Service
public class MissionService {

  private static final Logger logger = LoggerFactory.getLogger(MissionService.class);
  private static final Integer DEFAULT_EXPERIENCE_REWARD = 100;

  private final MissionRepository missionRepository;
  private final UserJourneyPurchaseRepository userJourneyPurchaseRepository;

  public MissionService(
      MissionRepository missionRepository,
      UserJourneyPurchaseRepository userJourneyPurchaseRepository) {
    this.missionRepository = missionRepository;
    this.userJourneyPurchaseRepository = userJourneyPurchaseRepository;
  }

  /**
   * Get mission details by ID with access control.
   *
   * @param journeyId Journey ID (for validation)
   * @param missionId Mission ID
   * @param userId User ID (null if not authenticated)
   * @return MissionDetailResponse
   * @throws MissionNotFoundException if mission not found or doesn't belong to the journey
   * @throws ForbiddenException if user doesn't have access to the mission
   */
  @Transactional(readOnly = true)
  public MissionDetailResponse getMissionDetail(Long journeyId, Long missionId, Long userId) {
    logger.debug(
        "Fetching mission details for missionId: {}, journeyId: {}, userId: {}",
        missionId,
        journeyId,
        userId);

    Mission mission =
        missionRepository
            .findByIdWithDetails(missionId)
            .orElseThrow(() -> new MissionNotFoundException(missionId));

    // Validate mission belongs to the specified journey
    Long actualJourneyId = mission.getChapter().getJourney().getId();
    if (!actualJourneyId.equals(journeyId)) {
      logger.warn(
          "Mission {} does not belong to journey {}. Actual journey: {}",
          missionId,
          journeyId,
          actualJourneyId);
      throw new MissionNotFoundException("Mission not found in the specified journey");
    }

    // Check access permissions
    checkMissionAccess(mission, userId);

    logger.info("Successfully fetched mission: {} for user: {}", missionId, userId);

    return mapToMissionDetailResponse(mission);
  }

  private void checkMissionAccess(Mission mission, Long userId) {
    MissionAccessLevel accessLevel = mission.getAccessLevel();

    // PUBLIC missions are accessible to everyone
    if (accessLevel == MissionAccessLevel.PUBLIC) {
      return;
    }

    // For AUTHENTICATED and PURCHASED, user must be logged in
    if (userId == null) {
      throw new waterballsa.exception.UnauthorizedException("Unauthorized or invalid token");
    }

    // AUTHENTICATED missions only require login
    if (accessLevel == MissionAccessLevel.AUTHENTICATED) {
      return;
    }

    // PURCHASED missions require journey purchase
    // For MVP: All authenticated users are considered to have purchased
    // TODO: Implement actual purchase check when payment system is ready
    if (accessLevel == MissionAccessLevel.PURCHASED) {
      // Uncomment when purchase system is implemented:
      // Long journeyId = mission.getChapter().getJourney().getId();
      // boolean hasPurchased = userJourneyPurchaseRepository.existsByUserIdAndJourneyId(userId,
      // journeyId);
      // if (!hasPurchased) {
      //   throw new ForbiddenException("You need to purchase this journey to access this mission");
      // }

      // For now, all authenticated users can access PURCHASED missions
      return;
    }
  }

  private MissionDetailResponse mapToMissionDetailResponse(Mission mission) {
    Long chapterId = mission.getChapter().getId();
    Long journeyId = mission.getChapter().getJourney().getId();
    Long createdAtMillis =
        mission.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    List<MissionContentDTO> contents =
        mission.getContents().stream()
            .filter(content -> !content.isDeleted())
            .sorted(Comparator.comparing(MissionContent::getContentOrder))
            .map(this::mapToMissionContentDTO)
            .collect(Collectors.toList());

    // Calculate video length from first video content
    String videoLength = calculateVideoLength(contents);

    MissionRewardDTO reward = new MissionRewardDTO(DEFAULT_EXPERIENCE_REWARD);

    return new MissionDetailResponse(
        mission.getId(),
        chapterId,
        journeyId,
        mission.getType().name(),
        mission.getTitle(),
        mission.getDescription(),
        mission.getAccessLevel().name(),
        createdAtMillis,
        videoLength,
        reward,
        contents);
  }

  private MissionContentDTO mapToMissionContentDTO(MissionContent content) {
    return new MissionContentDTO(
        content.getId(),
        content.getContentType().name().toLowerCase(),
        content.getContentUrl(),
        content.getDurationSeconds());
  }

  private String calculateVideoLength(List<MissionContentDTO> contents) {
    // Find first video content with duration
    Integer totalSeconds =
        contents.stream()
            .filter(c -> "video".equals(c.type()) && c.durationSeconds() != null)
            .map(MissionContentDTO::durationSeconds)
            .findFirst()
            .orElse(null);

    if (totalSeconds == null) {
      return null;
    }

    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }
}
