package waterballsa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import waterballsa.entity.UserJourneyEntity;

@Repository
public interface UserJourneyRepository extends JpaRepository<UserJourneyEntity, Long> {

  /**
   * Check if user has already purchased the journey.
   *
   * @param userId User ID
   * @param journeyId Journey ID
   * @return true if purchased, false otherwise
   */
  boolean existsByUserIdAndJourneyId(Long userId, Long journeyId);

  /**
   * Find all purchased journeys for a user (only from PAID orders). Results are ordered by purchase
   * time descending (most recent first).
   *
   * @param userId User ID
   * @return List of UserJourney
   */
  @Query(
      "SELECT uj FROM UserJourney uj, Order o "
          + "WHERE uj.userId = :userId "
          + "AND uj.orderId = o.id "
          + "AND uj.deletedAt IS NULL "
          + "AND o.status = 'PAID' "
          + "ORDER BY uj.purchasedAt DESC")
  List<UserJourneyEntity> findPurchasedJourneysByUserId(@Param("userId") Long userId);
}
