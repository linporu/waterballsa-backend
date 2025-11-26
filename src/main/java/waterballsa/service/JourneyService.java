package waterballsa.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
import waterballsa.dto.UserStatusDTO;
import waterballsa.entity.Chapter;
import waterballsa.entity.Journey;
import waterballsa.entity.Mission;
import waterballsa.entity.Order;
import waterballsa.entity.OrderStatus;
import waterballsa.exception.JourneyNotFoundException;
import waterballsa.repository.JourneyRepository;
import waterballsa.repository.OrderRepository;
import waterballsa.repository.UserJourneyRepository;

@Service
public class JourneyService {

  private static final Logger logger = LoggerFactory.getLogger(JourneyService.class);

  private final JourneyRepository journeyRepository;
  private final UserJourneyRepository userJourneyRepository;
  private final OrderRepository orderRepository;

  public JourneyService(
      JourneyRepository journeyRepository,
      UserJourneyRepository userJourneyRepository,
      OrderRepository orderRepository) {
    this.journeyRepository = journeyRepository;
    this.userJourneyRepository = userJourneyRepository;
    this.orderRepository = orderRepository;
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
   * @param userId Optional user ID for authenticated users
   * @return JourneyDetailResponse
   * @throws JourneyNotFoundException if journey not found or deleted
   */
  @Transactional(readOnly = true)
  public JourneyDetailResponse getJourneyDetail(Long journeyId, Optional<Long> userId) {
    logger.debug("Fetching journey details for journeyId: {}", journeyId);

    Journey journey =
        journeyRepository
            .findByIdWithChapters(journeyId)
            .orElseThrow(() -> new JourneyNotFoundException(journeyId));

    logger.info(
        "Successfully fetched journey: {} with {} chapters",
        journeyId,
        journey.getChapters().size());

    UserStatusDTO userStatus = userId.map(uid -> calculateUserStatus(uid, journeyId)).orElse(null);

    return mapToJourneyDetailResponse(journey, userStatus);
  }

  private JourneyDetailResponse mapToJourneyDetailResponse(
      Journey journey, UserStatusDTO userStatus) {
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
        userStatus,
        chapters);
  }

  private UserStatusDTO calculateUserStatus(Long userId, Long journeyId) {
    logger.debug("Calculating user status for userId: {} and journeyId: {}", userId, journeyId);

    boolean hasPurchased = userJourneyRepository.existsByUserIdAndJourneyId(userId, journeyId);

    Optional<Order> unpaidOrder =
        orderRepository.findByUserIdAndStatusAndJourneyId(userId, OrderStatus.UNPAID, journeyId);

    boolean hasUnpaidOrder = unpaidOrder.isPresent();
    Long unpaidOrderId = unpaidOrder.map(Order::getId).orElse(null);

    logger.debug(
        "User status calculated - hasPurchased: {}, hasUnpaidOrder: {}, unpaidOrderId: {}",
        hasPurchased,
        hasUnpaidOrder,
        unpaidOrderId);

    return new UserStatusDTO(hasPurchased, hasUnpaidOrder, unpaidOrderId);
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
