package waterballsa.exception;

public class OrderExpiredException extends RuntimeException {
  public OrderExpiredException(String message) {
    super(message);
  }

  public OrderExpiredException(Long orderId) {
    super("Order has expired: " + orderId);
  }
}
