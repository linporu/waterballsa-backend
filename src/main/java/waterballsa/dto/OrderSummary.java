package waterballsa.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderSummary(
    Long id,
    String orderNumber,
    String status,
    BigDecimal price,
    List<OrderItemSummary> items,
    Long createdAt,
    Long paidAt) {}
