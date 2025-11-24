package waterballsa.service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import waterballsa.dto.CreateOrderRequest;
import waterballsa.dto.OrderResponse;
import waterballsa.dto.PayOrderResponse;
import waterballsa.entity.*;
import waterballsa.exception.*;
import waterballsa.repository.*;
import waterballsa.util.OrderNumberGenerator;

@Service
public class OrderService {

  private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

  private final OrderRepository orderRepository;
  private final JourneyRepository journeyRepository;
  private final UserJourneyRepository userJourneyRepository;
  private final UserRepository userRepository;

  public OrderService(
      OrderRepository orderRepository,
      JourneyRepository journeyRepository,
      UserJourneyRepository userJourneyRepository,
      UserRepository userRepository) {
    this.orderRepository = orderRepository;
    this.journeyRepository = journeyRepository;
    this.userJourneyRepository = userJourneyRepository;
    this.userRepository = userRepository;
  }

  /**
   * Result of order creation operation.
   *
   * <p>This sealed interface represents the possible outcomes when creating an order:
   *
   * <ul>
   *   <li>{@link Created} - A new order was created
   *   <li>{@link Existing} - An existing unpaid order was returned
   * </ul>
   */
  public sealed interface OrderCreationResult {

    OrderResponse orderResponse();

    /** A new order was successfully created. */
    record Created(OrderResponse orderResponse) implements OrderCreationResult {}

    /** An existing unpaid order was returned (idempotent behavior). */
    record Existing(OrderResponse orderResponse) implements OrderCreationResult {}
  }

  /**
   * Create a new order for the user.
   *
   * @param userId User ID from JWT token
   * @param request Create order request
   * @return OrderCreationResult - either Created (new order) or Existing (unpaid order returned)
   * @throws JourneyAlreadyPurchasedException if user already purchased the journey
   * @throws JourneyNotFoundException if journey not found
   * @throws InvalidJourneyIdException if journey ID is invalid
   */
  @Transactional
  public OrderCreationResult createOrder(
      @org.springframework.lang.NonNull Long userId, CreateOrderRequest request) {
    logger.debug("Creating order for user: {}", userId);

    // Acquire pessimistic lock on user to prevent concurrent order creation
    userRepository
        .findByIdForUpdate(userId)
        .orElseThrow(() -> new UnauthorizedException("User not found"));

    // Validate request
    if (request.items().isEmpty()) {
      throw new InvalidJourneyIdException("Order must contain at least one item");
    }

    CreateOrderRequest.OrderItemRequest itemRequest = request.items().get(0);
    Long journeyId = itemRequest.journeyId();

    // Validate journey ID
    if (journeyId == null || journeyId <= 0) {
      throw new InvalidJourneyIdException(journeyId);
    }

    // Check if user already purchased this journey
    if (userJourneyRepository.existsByUserIdAndJourneyId(userId, journeyId)) {
      logger.warn("User {} already purchased journey {}", userId, journeyId);
      throw new JourneyAlreadyPurchasedException(userId, journeyId);
    }

    // Check if user already has an unpaid order for this journey
    var existingOrder =
        orderRepository.findByUserIdAndStatusAndJourneyId(userId, OrderStatus.UNPAID, journeyId);
    if (existingOrder.isPresent()) {
      logger.info("Returning existing unpaid order for user {} and journey {}", userId, journeyId);
      return new OrderCreationResult.Existing(mapToOrderResponse(existingOrder.get()));
    }

    // Fetch journey to lock price
    Journey journey =
        journeyRepository
            .findByIdAndDeletedAtIsNull(journeyId)
            .orElseThrow(() -> new JourneyNotFoundException(journeyId));

    // Create order
    String orderNumber = OrderNumberGenerator.generate(userId);
    BigDecimal journeyPrice = journey.getPrice();
    BigDecimal discount = new BigDecimal("0.00");

    Order order = new Order(orderNumber, userId, journeyPrice, discount);

    // Create order item with locked price
    OrderItem orderItem = new OrderItem(journeyId, itemRequest.quantity(), journeyPrice, discount);
    order.addItem(orderItem);

    // Save order
    order = orderRepository.save(order);

    logger.info("Successfully created order {} for user {}", order.getId(), userId);
    return new OrderCreationResult.Created(mapToOrderResponse(order));
  }

  /**
   * Get order detail by order ID.
   *
   * @param orderId Order ID
   * @param userId User ID from JWT token
   * @return OrderResponse
   * @throws OrderNotFoundException if order not found or user doesn't own the order
   */
  @Transactional(readOnly = true)
  public OrderResponse getOrderDetail(Long orderId, Long userId) {
    logger.debug("Fetching order {} for user {}", orderId, userId);

    Order order =
        orderRepository
            .findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

    logger.info("Successfully fetched order {} for user {}", orderId, userId);
    return mapToOrderResponse(order);
  }

  /**
   * Pay for an order.
   *
   * @param orderId Order ID
   * @param userId User ID from JWT token
   * @return PayOrderResponse
   * @throws OrderNotFoundException if order not found or user doesn't own the order
   * @throws OrderAlreadyPaidException if order already paid
   */
  @Transactional
  public PayOrderResponse payOrder(Long orderId, Long userId) {
    logger.debug("Processing payment for order {} by user {}", orderId, userId);

    // Fetch order and verify ownership
    Order order =
        orderRepository
            .findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

    // Check if already paid
    if (order.isPaid()) {
      logger.warn("Order {} is already paid", orderId);
      throw new OrderAlreadyPaidException(orderId);
    }

    // Mark order as paid
    order.markAsPaid();
    order = orderRepository.save(order);

    // Insert purchase records into user_journeys table to grant access
    for (OrderItem item : order.getItems()) {
      UserJourney userJourney =
          new UserJourney(userId, item.getJourneyId(), orderId, order.getPaidAt());
      userJourneyRepository.save(userJourney);
      logger.info("Granted journey {} access to user {}", item.getJourneyId(), userId);
    }

    logger.info("Successfully completed payment for order {}", orderId);

    Long paidAtMillis = order.getPaidAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    return new PayOrderResponse(
        order.getId(),
        order.getOrderNumber(),
        order.getStatus().name(),
        order.getPrice(),
        paidAtMillis,
        "付款完成");
  }

  private OrderResponse mapToOrderResponse(Order order) {
    // Get username
    String username =
        userRepository.findById(order.getUserId()).map(User::getUsername).orElse("Unknown User");

    // Convert timestamps to milliseconds
    Long createdAtMillis =
        order.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    Long paidAtMillis =
        order.getPaidAt() != null
            ? order.getPaidAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            : null;

    // Map order items
    List<OrderResponse.OrderItemResponse> items =
        order.getItems().stream()
            .map(
                item -> {
                  // Fetch journey title
                  String journeyTitle =
                      journeyRepository
                          .findByIdAndDeletedAtIsNull(item.getJourneyId())
                          .map(Journey::getTitle)
                          .orElse("Unknown Journey");

                  return new OrderResponse.OrderItemResponse(
                      item.getJourneyId(),
                      journeyTitle,
                      item.getQuantity(),
                      item.getOriginalPrice(),
                      item.getDiscount(),
                      item.getPrice());
                })
            .collect(Collectors.toList());

    return new OrderResponse(
        order.getId(),
        order.getOrderNumber(),
        order.getUserId(),
        username,
        order.getStatus().name(),
        order.getOriginalPrice(),
        order.getDiscount(),
        order.getPrice(),
        items,
        createdAtMillis,
        paidAtMillis);
  }
}
