package waterballsa.exception;

public class JourneyNotFoundException extends RuntimeException {
  public JourneyNotFoundException(String message) {
    super(message);
  }

  public JourneyNotFoundException(Long journeyId) {
    super("Journey not found: " + journeyId);
  }
}
