package waterballsa.exception;

public class InvalidJourneyIdException extends RuntimeException {
  public InvalidJourneyIdException(String message) {
    super(message);
  }

  public InvalidJourneyIdException(Long journeyId) {
    super("Invalid journey ID: " + journeyId);
  }
}
