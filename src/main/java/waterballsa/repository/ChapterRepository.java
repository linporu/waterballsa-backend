package waterballsa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import waterballsa.entity.Chapter;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {}
