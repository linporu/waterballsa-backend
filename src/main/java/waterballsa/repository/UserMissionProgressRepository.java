package waterballsa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import waterballsa.entity.UserMissionProgress;

@Repository
public interface UserMissionProgressRepository extends JpaRepository<UserMissionProgress, Long> {

  Optional<UserMissionProgress> findByUserIdAndMissionIdAndDeletedAtIsNull(
      Long userId, Long missionId);
}
