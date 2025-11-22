package waterballsa.util;

import waterballsa.exception.UnauthorizedException;

public class AuthenticationValidator {

  private AuthenticationValidator() {
    // Utility class - prevent instantiation
  }

  /**
   * Validates that the user is authenticated by checking if userId is not null.
   *
   * @param userId the user ID to validate
   * @throws UnauthorizedException if userId is null
   */
  public static void validateUserAuthenticated(Long userId) {
    if (userId == null) {
      throw new UnauthorizedException("Unauthorized or invalid token");
    }
  }
}
