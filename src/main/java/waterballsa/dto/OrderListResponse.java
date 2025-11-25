package waterballsa.dto;

import java.util.List;

public record OrderListResponse(List<OrderSummary> orders, Pagination pagination) {}
