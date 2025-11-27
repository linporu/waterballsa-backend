package waterballsa.validator;

import org.springframework.stereotype.Component;
import waterballsa.dto.CreateOrderRequest;
import waterballsa.entity.Journey;
import waterballsa.entity.Order;
import waterballsa.entity.User;
import waterballsa.exception.InvalidJourneyIdException;
import waterballsa.exception.JourneyAlreadyPurchasedException;
import waterballsa.exception.JourneyNotFoundException;
import waterballsa.exception.OrderAlreadyPaidException;
import waterballsa.exception.OrderExpiredException;
import waterballsa.exception.UnauthorizedException;
import waterballsa.repository.JourneyRepository;
import waterballsa.repository.UserJourneyRepository;
import waterballsa.repository.UserRepository;

/**
 * Validator for order-related operations.
 *
 * <p>This validator handles all validation logic related to orders, including order request
 * validation, journey purchase validation, and order status validation.
 */
@Component
public class OrderValidator {

  private final UserJourneyRepository userJourneyRepository;
  private final JourneyRepository journeyRepository;
  private final UserRepository userRepository;

  public OrderValidator(
      UserJourneyRepository userJourneyRepository,
      JourneyRepository journeyRepository,
      UserRepository userRepository) {
    this.userJourneyRepository = userJourneyRepository;
    this.journeyRepository = journeyRepository;
    this.userRepository = userRepository;
  }

  /**
   * Validates that the user exists and locks it for pessimistic locking.
   *
   * <p>This method is used to prevent concurrent order creation by the same user.
   *
   * @param userId the user ID
   * @return the locked User entity
   * @throws UnauthorizedException if user not found
   */
  public User validateAndLockUser(Long userId) {
    return userRepository
        .findByIdForUpdate(userId)
        .orElseThrow(() -> new UnauthorizedException("User not found"));
  }

  /**
   * Validates that the order request is valid.
   *
   * <p>This method checks:
   *
   * <ul>
   *   <li>Order contains at least one item
   *   <li>Journey ID is not null and positive
   * </ul>
   *
   * @param request the create order request
   * @throws InvalidJourneyIdException if request is invalid
   */
  public void validateOrderRequest(CreateOrderRequest request) {
    if (request.items().isEmpty()) {
      throw new InvalidJourneyIdException("Order must contain at least one item");
    }

    CreateOrderRequest.OrderItemRequest itemRequest = request.items().get(0);
    Long journeyId = itemRequest.journeyId();

    if (journeyId == null || journeyId <= 0) {
      throw new InvalidJourneyIdException(journeyId);
    }
  }

  /**
   * Validates that the user has not already purchased the journey.
   *
   * @param userId the user ID
   * @param journeyId the journey ID
   * @throws JourneyAlreadyPurchasedException if user already purchased the journey
   */
  public void validateJourneyNotPurchased(Long userId, Long journeyId) {
    if (userJourneyRepository.existsByUserIdAndJourneyId(userId, journeyId)) {
      throw new JourneyAlreadyPurchasedException(userId, journeyId);
    }
  }

  /**
   * Validates that the journey exists and returns it.
   *
   * <p>This method combines validation and retrieval to avoid duplicate database queries.
   *
   * @param journeyId the journey ID
   * @return the validated Journey entity
   * @throws JourneyNotFoundException if journey not found
   */
  public Journey validateAndGetJourney(Long journeyId) {
    return journeyRepository
        .findByIdAndDeletedAtIsNull(journeyId)
        .orElseThrow(() -> new JourneyNotFoundException(journeyId));
  }

  /**
   * Validates that the order has not been paid.
   *
   * @param order the order
   * @throws OrderAlreadyPaidException if order already paid
   */
  public void validateOrderNotPaid(Order order) {
    if (order.isPaid()) {
      throw new OrderAlreadyPaidException(order.getId());
    }
  }

  /**
   * Validates that the order has not expired.
   *
   * @param order the order
   * @throws OrderExpiredException if order has expired
   */
  public void validateOrderNotExpired(Order order) {
    if (order.isExpired()) {
      throw new OrderExpiredException(order.getId());
    }
  }
}
