package waterballsa.service;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import waterballsa.dto.MissionDetailResponse;
import waterballsa.dto.MissionResourceDTO;
import waterballsa.dto.MissionRewardDTO;
import waterballsa.entity.MissionEntity;
import waterballsa.entity.MissionResourceEntity;
import waterballsa.exception.ForbiddenException;
import waterballsa.exception.MissionNotFoundException;
import waterballsa.repository.MissionRepository;
import waterballsa.validator.MissionAccessValidator;

@Service
public class MissionService {

  private static final Logger logger = LoggerFactory.getLogger(MissionService.class);
  private static final Integer DEFAULT_EXPERIENCE_REWARD = 100;

  private final MissionAccessValidator missionAccessValidator;
  private final MissionRepository missionRepository;

  public MissionService(
      MissionAccessValidator missionAccessValidator, MissionRepository missionRepository) {
    this.missionAccessValidator = missionAccessValidator;
    this.missionRepository = missionRepository;
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

    MissionEntity mission =
        missionRepository
            .findByIdWithDetails(missionId)
            .orElseThrow(() -> new MissionNotFoundException(missionId));

    missionAccessValidator.validateMissionBelongsToJourney(mission, journeyId);
    missionAccessValidator.validateMissionAccess(mission, userId);

    logger.info("Successfully fetched mission: {} for user: {}", missionId, userId);

    return mapToMissionDetailResponse(mission);
  }

  // ==================== Helper Methods ====================

  private MissionDetailResponse mapToMissionDetailResponse(MissionEntity mission) {
    Long chapterId = mission.getChapter().getId();
    Long journeyId = mission.getChapter().getJourney().getId();
    Long createdAtMillis =
        mission.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    List<MissionResourceDTO> resources =
        mission.getResources().stream()
            .filter(resource -> !resource.isDeleted())
            .sorted(Comparator.comparing(MissionResourceEntity::getContentOrder))
            .map(this::mapToMissionResourceDTO)
            .collect(Collectors.toList());

    // Calculate video length from first video content
    String videoLength = calculateVideoLength(resources);

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
        resources);
  }

  private MissionResourceDTO mapToMissionResourceDTO(MissionResourceEntity resource) {
    return new MissionResourceDTO(
        resource.getId(),
        resource.getResourceType().name().toLowerCase(),
        resource.getResourceUrl(),
        resource.getResourceContent(),
        resource.getDurationSeconds());
  }

  private String calculateVideoLength(List<MissionResourceDTO> contents) {
    // Find first video content with duration
    Integer totalSeconds =
        contents.stream()
            .filter(c -> "video".equals(c.type()) && c.durationSeconds() != null)
            .map(MissionResourceDTO::durationSeconds)
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
