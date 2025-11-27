package waterballsa.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import waterballsa.entity.JourneyEntity;

@Repository
public interface JourneyRepository extends JpaRepository<JourneyEntity, Long> {

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
  Optional<JourneyEntity> findByIdWithChapters(@Param("id") Long id);

  Optional<JourneyEntity> findByIdAndDeletedAtIsNull(Long id);

  /**
   * Find all journeys that are not soft-deleted, ordered by creation time (oldest first).
   *
   * @return List of journeys
   */
  @Query("SELECT j FROM Journey j WHERE j.deletedAt IS NULL ORDER BY j.createdAt ASC")
  List<JourneyEntity> findAllNotDeleted();
}
