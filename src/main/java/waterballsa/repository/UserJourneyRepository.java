package waterballsa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import waterballsa.entity.UserJourney;

@Repository
public interface UserJourneyRepository extends JpaRepository<UserJourney, Long> {

  /**
   * Check if user has already purchased the journey.
   *
   * @param userId User ID
   * @param journeyId Journey ID
   * @return true if purchased, false otherwise
   */
  boolean existsByUserIdAndJourneyId(Long userId, Long journeyId);
}
