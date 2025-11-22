package waterballsa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import waterballsa.entity.MissionContent;

@Repository
public interface MissionContentRepository extends JpaRepository<MissionContent, Long> {}
