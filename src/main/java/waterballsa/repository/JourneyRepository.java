package waterballsa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import waterballsa.entity.Journey;

@Repository
public interface JourneyRepository extends JpaRepository<Journey, Long> {

  /**
   * Find journey by ID with chapters eagerly loaded.
   *
   * @param id Journey ID
   * @return Optional of Journey
   */
  @Query(
      "SELECT j FROM Journey j "
          + "LEFT JOIN FETCH j.chapters c "
          + "WHERE j.id = :id AND j.deletedAt IS NULL "
          + "AND c.deletedAt IS NULL "
          + "ORDER BY c.orderIndex")
  Optional<Journey> findByIdWithChapters(@Param("id") Long id);

  Optional<Journey> findByIdAndDeletedAtIsNull(Long id);
}
