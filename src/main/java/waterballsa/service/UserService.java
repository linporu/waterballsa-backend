package waterballsa.service;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import waterballsa.dto.OrderItemSummary;
import waterballsa.dto.OrderListResponse;
import waterballsa.dto.OrderSummary;
import waterballsa.dto.Pagination;
import waterballsa.dto.UserInfo;
import waterballsa.dto.UserJourneyItem;
import waterballsa.dto.UserJourneyListResponse;
import waterballsa.entity.JourneyEntity;
import waterballsa.entity.OrderEntity;
import waterballsa.entity.UserEntity;
import waterballsa.entity.UserJourneyEntity;
import waterballsa.exception.JourneyNotFoundException;
import waterballsa.exception.OrderNotFoundException;
import waterballsa.exception.UserNotFoundException;
import waterballsa.repository.JourneyRepository;
import waterballsa.repository.OrderRepository;
import waterballsa.repository.UserJourneyRepository;
import waterballsa.repository.UserRepository;
import waterballsa.validator.UserAccessValidator;

@Service
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  private final UserAccessValidator userAccessValidator;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final UserJourneyRepository userJourneyRepository;
  private final JourneyRepository journeyRepository;

  public UserService(
      UserAccessValidator userAccessValidator,
      UserRepository userRepository,
      OrderRepository orderRepository,
      UserJourneyRepository userJourneyRepository,
      JourneyRepository journeyRepository) {
    this.userAccessValidator = userAccessValidator;
    this.userRepository = userRepository;
    this.orderRepository = orderRepository;
    this.userJourneyRepository = userJourneyRepository;
    this.journeyRepository = journeyRepository;
  }

  @Transactional(readOnly = true)
  public UserInfo getUserById(Long userId) {
    logger.debug("Fetching user profile for user ID: {}", userId);

    UserEntity user =
        userRepository
            .findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(
                () -> {
                  logger.warn("User not found with ID: {}", userId);
                  return new UserNotFoundException("User not found");
                });

    logger.debug("Successfully fetched user profile for user ID: {}", userId);

    return new UserInfo(
        user.getId(),
        user.getUsername(),
        user.getExperiencePoints(),
        user.getLevel(),
        user.getRole());
  }

  /**
   * Get user's order list with pagination.
   *
   * @param userId User ID from path parameter
   * @param authenticatedUserId User ID from authentication token
   * @param page Page number (1-indexed)
   * @param limit Items per page
   * @return Order list response with pagination info
   */
  @Transactional(readOnly = true)
  public OrderListResponse getUserOrders(
      Long userId, Long authenticatedUserId, int page, int limit) {
    logger.debug(
        "Fetching orders for user {} (authenticated user: {}, page: {}, limit: {})",
        userId,
        authenticatedUserId,
        page,
        limit);

    userAccessValidator.validateSelfAccess(userId, authenticatedUserId);

    // Fetch paginated orders (Spring Data uses 0-indexed pages)
    Pageable pageable = PageRequest.of(page - 1, limit);
    Page<OrderEntity> orderPage =
        orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

    // Map to DTOs
    List<OrderSummary> orderSummaries =
        orderPage.getContent().stream().map(this::mapToOrderSummary).collect(Collectors.toList());

    // Create pagination info
    Pagination pagination = new Pagination(page, limit, orderPage.getTotalElements());

    logger.info(
        "Successfully fetched {} orders for user {} (page {}/{})",
        orderSummaries.size(),
        userId,
        page,
        orderPage.getTotalPages());

    return new OrderListResponse(orderSummaries, pagination);
  }

  /**
   * Get user's purchased journeys (only from PAID orders).
   *
   * @param userId User ID from path parameter
   * @param authenticatedUserId User ID from authentication token
   * @return User journey list response
   */
  @Transactional(readOnly = true)
  public UserJourneyListResponse getUserJourneys(Long userId, Long authenticatedUserId) {
    logger.debug(
        "Fetching purchased journeys for user {} (authenticated user: {})",
        userId,
        authenticatedUserId);

    userAccessValidator.validateSelfAccess(userId, authenticatedUserId);

    // Fetch purchased journeys (only PAID orders)
    List<UserJourneyEntity> userJourneys =
        userJourneyRepository.findPurchasedJourneysByUserId(userId);

    // Map to DTOs
    List<UserJourneyItem> journeyItems =
        userJourneys.stream().map(this::mapToUserJourneyItem).collect(Collectors.toList());

    logger.info(
        "Successfully fetched {} purchased journeys for user {}", journeyItems.size(), userId);

    return new UserJourneyListResponse(journeyItems);
  }

  // ==================== Helper Methods ====================

  /**
   * Map Order entity to OrderSummary DTO.
   *
   * @param order Order entity
   * @return OrderSummary DTO
   */
  private OrderSummary mapToOrderSummary(OrderEntity order) {
    Long createdAtMillis =
        order.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    Long paidAtMillis =
        order.getPaidAt() != null
            ? order.getPaidAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            : null;

    List<OrderItemSummary> items =
        order.getItems().stream()
            .map(
                item -> {
                  String journeyTitle =
                      journeyRepository
                          .findByIdAndDeletedAtIsNull(item.getJourneyId())
                          .map(JourneyEntity::getTitle)
                          .orElse("Unknown Journey");
                  return new OrderItemSummary(item.getJourneyId(), journeyTitle);
                })
            .collect(Collectors.toList());

    return new OrderSummary(
        order.getId(),
        order.getOrderNumber(),
        order.getStatus().name(),
        order.getPrice(),
        items,
        createdAtMillis,
        paidAtMillis);
  }

  /**
   * Map UserJourney entity to UserJourneyItem DTO.
   *
   * @param userJourney UserJourney entity
   * @return UserJourneyItem DTO
   */
  @SuppressWarnings("null")
  private UserJourneyItem mapToUserJourneyItem(UserJourneyEntity userJourney) {
    JourneyEntity journey =
        journeyRepository
            .findByIdAndDeletedAtIsNull(userJourney.getJourneyId())
            .orElseThrow(() -> new JourneyNotFoundException(userJourney.getJourneyId()));

    OrderEntity order =
        orderRepository
            .findById(userJourney.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(userJourney.getOrderId()));

    Long purchasedAtMillis =
        userJourney.getPurchasedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    return new UserJourneyItem(
        journey.getId(),
        journey.getTitle(),
        journey.getSlug(),
        journey.getCoverImageUrl(),
        journey.getTeacherName(),
        purchasedAtMillis,
        order.getOrderNumber());
  }
}
