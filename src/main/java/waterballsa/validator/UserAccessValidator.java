package waterballsa.validator;

import org.springframework.stereotype.Component;
import waterballsa.exception.UserNotFoundException;

/**
 * Validator for user access control.
 *
 * <p>This validator handles user access validation, ensuring that users can only access their own
 * resources (e.g., their own orders, journeys, progress).
 */
@Component
public class UserAccessValidator {

  private final AuthValidator authValidator;

  public UserAccessValidator(AuthValidator authValidator) {
    this.authValidator = authValidator;
  }

  /**
   * Validates that a user can only access their own resources.
   *
   * <p>This method ensures that:
   *
   * <ol>
   *   <li>The current user is authenticated
   *   <li>The path user ID matches the authenticated user ID
   * </ol>
   *
   * @param pathUserId the user ID from the path parameter
   * @param currentUserId the authenticated user ID from the JWT token
   * @throws UserNotFoundException if the user attempts to access another user's resources
   */
  public void validateSelfAccess(Long pathUserId, Long currentUserId) {
    authValidator.validateUserAuthenticated(currentUserId);

    if (!pathUserId.equals(currentUserId)) {
      throw new UserNotFoundException("User not found");
    }
  }
}
