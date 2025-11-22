package waterballsa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import waterballsa.entity.Mission;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

  /**
   * Find mission by ID with chapter, journey and contents eagerly loaded.
   *
   * @param id Mission ID
   * @return Optional of Mission
   */
  @Query(
      "SELECT DISTINCT m FROM Mission m "
          + "LEFT JOIN FETCH m.chapter c "
          + "LEFT JOIN FETCH c.journey j "
          + "LEFT JOIN FETCH m.contents mc "
          + "WHERE m.id = :id AND m.deletedAt IS NULL "
          + "AND c.deletedAt IS NULL "
          + "AND j.deletedAt IS NULL "
          + "AND mc.deletedAt IS NULL "
          + "ORDER BY mc.contentOrder")
  Optional<Mission> findByIdWithDetails(@Param("id") Long id);

  Optional<Mission> findByIdAndDeletedAtIsNull(Long id);
}
