package waterballsa.exception;

public class JourneyAlreadyPurchasedException extends RuntimeException {
  public JourneyAlreadyPurchasedException(String message) {
    super(message);
  }

  public JourneyAlreadyPurchasedException(Long userId, Long journeyId) {
    super("Journey already purchased by user " + userId + ": " + journeyId);
  }
}
