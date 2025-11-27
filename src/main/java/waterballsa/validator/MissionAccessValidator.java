package waterballsa.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import waterballsa.entity.MissionAccessLevelEntity;
import waterballsa.entity.MissionEntity;
import waterballsa.exception.ForbiddenException;
import waterballsa.exception.MissionNotFoundException;
import waterballsa.repository.UserJourneyRepository;

/**
 * Validator for mission access control.
 *
 * <p>This validator handles mission access validation, ensuring that users have appropriate access
 * levels to view mission content based on the mission's access level (PUBLIC, AUTHENTICATED,
 * PURCHASED).
 */
@Component
public class MissionAccessValidator {

  private static final Logger logger = LoggerFactory.getLogger(MissionAccessValidator.class);

  private final AuthValidator authValidator;
  private final UserJourneyRepository userJourneyRepository;

  public MissionAccessValidator(
      AuthValidator authValidator, UserJourneyRepository userJourneyRepository) {
    this.authValidator = authValidator;
    this.userJourneyRepository = userJourneyRepository;
  }

  /**
   * Validates that the mission belongs to the specified journey.
   *
   * @param mission the mission
   * @param journeyId the expected journey ID
   * @throws MissionNotFoundException if mission doesn't belong to the journey
   */
  public void validateMissionBelongsToJourney(MissionEntity mission, Long journeyId) {
    Long actualJourneyId = mission.getChapter().getJourney().getId();
    if (!actualJourneyId.equals(journeyId)) {
      logger.warn(
          "Mission {} does not belong to journey {}. Actual journey: {}",
          mission.getId(),
          journeyId,
          actualJourneyId);
      throw new MissionNotFoundException("Mission not found in the specified journey");
    }
  }

  /**
   * Validates that the user has access to the mission based on its access level.
   *
   * <p>Access levels:
   *
   * <ul>
   *   <li>PUBLIC: Anyone can access
   *   <li>AUTHENTICATED: User must be logged in
   *   <li>PURCHASED: User must have purchased the journey
   * </ul>
   *
   * @param mission the mission
   * @param userId the user ID (can be null for anonymous users)
   * @throws waterballsa.exception.UnauthorizedException if authentication required but user not
   *     logged in
   * @throws ForbiddenException if purchase required but user hasn't purchased
   */
  public void validateMissionAccess(MissionEntity mission, Long userId) {
    MissionAccessLevelEntity accessLevel = mission.getAccessLevel();

    // PUBLIC missions are accessible to everyone
    if (accessLevel == MissionAccessLevelEntity.PUBLIC) {
      return;
    }

    // For AUTHENTICATED and PURCHASED, user must be logged in
    authValidator.validateUserAuthenticated(userId);

    // AUTHENTICATED missions only require login
    if (accessLevel == MissionAccessLevelEntity.AUTHENTICATED) {
      return;
    }

    // PURCHASED missions require journey purchase
    if (accessLevel == MissionAccessLevelEntity.PURCHASED) {
      Long journeyId = mission.getChapter().getJourney().getId();
      boolean hasPurchased = userJourneyRepository.existsByUserIdAndJourneyId(userId, journeyId);
      if (!hasPurchased) {
        throw new ForbiddenException("You need to purchase this journey to access this mission");
      }
      return;
    }
  }
}
