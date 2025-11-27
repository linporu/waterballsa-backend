package waterballsa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import waterballsa.entity.MissionResourceEntity;

@Repository
public interface MissionResourceRepository extends JpaRepository<MissionResourceEntity, Long> {}
