package waterballsa.dto;

import java.math.BigDecimal;

public record PayOrderResponse(
    Long id, String orderNumber, String status, BigDecimal price, Long paidAt, String message) {}
