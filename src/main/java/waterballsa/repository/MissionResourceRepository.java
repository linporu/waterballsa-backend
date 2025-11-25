package waterballsa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import waterballsa.entity.MissionResource;

@Repository
public interface MissionResourceRepository extends JpaRepository<MissionResource, Long> {}
