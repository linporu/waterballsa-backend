package waterballsa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import waterballsa.dto.UserInfo;
import waterballsa.entity.User;
import waterballsa.exception.UserNotFoundException;
import waterballsa.repository.UserRepository;

@Service
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public UserInfo getUserById(Long userId) {
    logger.debug("Fetching user profile for user ID: {}", userId);

    User user =
        userRepository
            .findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(
                () -> {
                  logger.warn("User not found with ID: {}", userId);
                  return new UserNotFoundException("User not found");
                });

    logger.debug("Successfully fetched user profile for user ID: {}", userId);

    return new UserInfo(
        user.getId(),
        user.getUsername(),
        user.getExperiencePoints(),
        user.getLevel(),
        user.getRole());
  }
}
