package waterballsa.validator;

import org.springframework.stereotype.Component;
import waterballsa.exception.UnauthorizedException;

/**
 * Validator for authentication-related checks.
 *
 * <p>This validator handles authentication validation logic, ensuring that users are properly
 * authenticated before accessing protected resources.
 */
@Component
public class AuthValidator {

  /**
   * Validates that the user is authenticated by checking if userId is not null.
   *
   * @param userId the user ID to validate
   * @throws UnauthorizedException if userId is null (user not authenticated)
   */
  public void validateUserAuthenticated(Long userId) {
    if (userId == null) {
      throw new UnauthorizedException("Unauthorized or invalid token");
    }
  }
}
