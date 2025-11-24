package waterballsa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record CreateOrderRequest(
    @NotEmpty(message = "Items cannot be empty") @Valid List<OrderItemRequest> items) {

  public record OrderItemRequest(
      @NotNull(message = "Journey ID is required")
          @Positive(message = "Journey ID must be positive")
          Long journeyId,
      @NotNull(message = "Quantity is required")
          @Min(value = 1, message = "Quantity must be at least 1")
          Integer quantity) {}
}
