package waterballsa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import waterballsa.entity.UserMissionProgressEntity;

@Repository
public interface UserMissionProgressRepository
    extends JpaRepository<UserMissionProgressEntity, Long> {

  Optional<UserMissionProgressEntity> findByUserIdAndMissionIdAndDeletedAtIsNull(
      Long userId, Long missionId);
}
