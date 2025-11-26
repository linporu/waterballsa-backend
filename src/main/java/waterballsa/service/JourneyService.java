package waterballsa.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import waterballsa.dto.ChapterDTO;
import waterballsa.dto.JourneyDetailResponse;
import waterballsa.dto.JourneyListItemDTO;
import waterballsa.dto.JourneyListResponse;
import waterballsa.dto.MissionSummaryDTO;
import waterballsa.entity.Chapter;
import waterballsa.entity.Journey;
import waterballsa.entity.Mission;
import waterballsa.exception.JourneyNotFoundException;
import waterballsa.repository.JourneyRepository;

@Service
public class JourneyService {

  private static final Logger logger = LoggerFactory.getLogger(JourneyService.class);

  private final JourneyRepository journeyRepository;

  public JourneyService(JourneyRepository journeyRepository) {
    this.journeyRepository = journeyRepository;
  }

  /**
   * Get all journeys ordered by creation time (oldest first).
   *
   * @return JourneyListResponse containing list of all non-deleted journeys
   */
  @Transactional(readOnly = true)
  public JourneyListResponse getJourneys() {
    logger.debug("Fetching all journeys");

    List<Journey> journeys = journeyRepository.findAllNotDeleted();

    logger.info("Successfully fetched {} journeys", journeys.size());

    List<JourneyListItemDTO> journeyList =
        journeys.stream().map(this::mapToJourneyListItemDTO).collect(Collectors.toList());

    return new JourneyListResponse(journeyList);
  }

  /**
   * Get journey details with chapters and missions.
   *
   * @param journeyId Journey ID
   * @return JourneyDetailResponse
   * @throws JourneyNotFoundException if journey not found or deleted
   */
  @Transactional(readOnly = true)
  public JourneyDetailResponse getJourneyDetail(Long journeyId) {
    logger.debug("Fetching journey details for journeyId: {}", journeyId);

    Journey journey =
        journeyRepository
            .findByIdWithChapters(journeyId)
            .orElseThrow(() -> new JourneyNotFoundException(journeyId));

    logger.info(
        "Successfully fetched journey: {} with {} chapters",
        journeyId,
        journey.getChapters().size());

    return mapToJourneyDetailResponse(journey);
  }

  private JourneyDetailResponse mapToJourneyDetailResponse(Journey journey) {
    List<ChapterDTO> chapters =
        journey.getChapters().stream()
            .filter(chapter -> !chapter.isDeleted())
            .sorted(Comparator.comparing(Chapter::getOrderIndex))
            .map(this::mapToChapterDTO)
            .collect(Collectors.toList());

    return new JourneyDetailResponse(
        journey.getId(),
        journey.getSlug(),
        journey.getTitle(),
        journey.getDescription(),
        journey.getCoverImageUrl(),
        journey.getTeacherName(),
        chapters);
  }

  private ChapterDTO mapToChapterDTO(Chapter chapter) {
    List<MissionSummaryDTO> missions =
        chapter.getMissions().stream()
            .filter(mission -> !mission.isDeleted())
            .sorted(Comparator.comparing(Mission::getOrderIndex))
            .map(this::mapToMissionSummaryDTO)
            .collect(Collectors.toList());

    return new ChapterDTO(chapter.getId(), chapter.getTitle(), chapter.getOrderIndex(), missions);
  }

  private MissionSummaryDTO mapToMissionSummaryDTO(Mission mission) {
    return new MissionSummaryDTO(
        mission.getId(),
        mission.getType().name(),
        mission.getTitle(),
        mission.getAccessLevel().name(),
        mission.getOrderIndex(),
        null); // status will be null for now (not implemented yet)
  }

  private JourneyListItemDTO mapToJourneyListItemDTO(Journey journey) {
    return new JourneyListItemDTO(
        journey.getId(),
        journey.getSlug(),
        journey.getTitle(),
        journey.getDescription(),
        journey.getCoverImageUrl(),
        journey.getTeacherName());
  }
}
