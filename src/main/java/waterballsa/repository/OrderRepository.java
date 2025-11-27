package waterballsa.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import waterballsa.entity.OrderEntity;
import waterballsa.entity.OrderStatusEntity;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

  /**
   * Find order by order number.
   *
   * @param orderNumber Order number
   * @return Optional of Order
   */
  Optional<OrderEntity> findByOrderNumber(String orderNumber);

  /**
   * Find order by ID and user ID (for ownership verification).
   *
   * @param id Order ID
   * @param userId User ID
   * @return Optional of Order
   */
  Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);

  /**
   * Find unpaid order by user ID and journey ID. Used to check if user already has an unpaid order
   * for the same journey. Returns the most recent order if multiple exist.
   *
   * @param userId User ID
   * @param status Order status
   * @param journeyId Journey ID
   * @return Optional of Order
   */
  @Query(
      "SELECT o FROM Order o "
          + "JOIN o.items oi "
          + "WHERE o.userId = :userId "
          + "AND o.status = :status "
          + "AND oi.journeyId = :journeyId "
          + "AND o.deletedAt IS NULL "
          + "ORDER BY o.createdAt DESC LIMIT 1")
  Optional<OrderEntity> findByUserIdAndStatusAndJourneyId(
      @Param("userId") Long userId,
      @Param("status") OrderStatusEntity status,
      @Param("journeyId") Long journeyId);

  /**
   * Find all orders by user ID with pagination, ordered by creation time descending (newest first).
   *
   * @param userId User ID
   * @param pageable Pagination parameters
   * @return Page of orders
   */
  @Query(
      "SELECT o FROM Order o "
          + "WHERE o.userId = :userId "
          + "AND o.deletedAt IS NULL "
          + "ORDER BY o.createdAt DESC")
  Page<OrderEntity> findByUserIdOrderByCreatedAtDesc(
      @Param("userId") Long userId, Pageable pageable);

  /**
   * Find orders by status where expiration time has passed. Used by scheduled task to expire unpaid
   * orders after 3 days.
   *
   * @param status Order status (typically UNPAID)
   * @param now Current time
   * @return List of orders that should be expired
   */
  List<OrderEntity> findByStatusAndExpiredAtBefore(OrderStatusEntity status, LocalDateTime now);

  /**
   * Find order by ID and user ID with pessimistic write lock for payment processing.
   *
   * <p>This prevents concurrent payment attempts on the same order by acquiring a database-level
   * write lock.
   *
   * @param id Order ID
   * @param userId User ID
   * @return Optional of Order
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT o FROM Order o WHERE o.id = :id AND o.userId = :userId AND o.deletedAt IS NULL")
  Optional<OrderEntity> findByIdAndUserIdForUpdate(
      @Param("id") Long id, @Param("userId") Long userId);
}
