package waterballsa.exception;

public class OrderAlreadyPaidException extends RuntimeException {
  public OrderAlreadyPaidException(String message) {
    super(message);
  }

  public OrderAlreadyPaidException(Long orderId) {
    super("Order already paid: " + orderId);
  }
}
