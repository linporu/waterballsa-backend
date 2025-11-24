package waterballsa.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import waterballsa.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsernameAndDeletedAtIsNull(String username);

  Optional<User> findByIdAndDeletedAtIsNull(Long id);

  boolean existsByUsernameAndDeletedAtIsNull(String username);

  /**
   * Find user by ID with pessimistic write lock for preventing concurrent order creation.
   *
   * @param id User ID
   * @return Optional of User
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
  Optional<User> findByIdForUpdate(@Param("id") Long id);
}
