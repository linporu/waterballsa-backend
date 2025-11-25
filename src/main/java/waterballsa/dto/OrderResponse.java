package waterballsa.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
    Long id,
    String orderNumber,
    Long userId,
    String username,
    String status,
    BigDecimal originalPrice,
    BigDecimal discount,
    BigDecimal price,
    List<OrderItemResponse> items,
    Long createdAt,
    Long paidAt,
    Long expiredAt) {

  public record OrderItemResponse(
      Long journeyId,
      String journeyTitle,
      Integer quantity,
      BigDecimal originalPrice,
      BigDecimal discount,
      BigDecimal price) {}
}
