package waterballsa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import waterballsa.entity.AccessTokenEntity;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessTokenEntity, Long> {

  Optional<AccessTokenEntity> findByTokenJti(String tokenJti);

  boolean existsByTokenJti(String tokenJti);
}
