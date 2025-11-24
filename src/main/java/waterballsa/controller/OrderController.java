package waterballsa.controller;

import jakarta.validation.Valid;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import waterballsa.dto.CreateOrderRequest;
import waterballsa.dto.OrderResponse;
import waterballsa.dto.PayOrderResponse;
import waterballsa.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

  private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  /**
   * Create a new order.
   *
   * @param request Create order request
   * @return Order response with 201 (new order) or 200 (existing unpaid order)
   */
  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    logger.debug("POST /orders request received");

    Long currentUserId = getCurrentUserId();
    var result = orderService.createOrder(currentUserId, request);

    if (result instanceof OrderService.OrderCreationResult.Created created) {
      logger.info("Successfully created order for user {}", currentUserId);
      return ResponseEntity.status(HttpStatus.CREATED).body(created.orderResponse());
    } else if (result instanceof OrderService.OrderCreationResult.Existing existing) {
      logger.info("Successfully returned existing unpaid order for user {}", currentUserId);
      return ResponseEntity.ok(existing.orderResponse());
    }

    // This should never happen due to sealed interface, but satisfies compiler
    throw new IllegalStateException("Unexpected OrderCreationResult type: " + result.getClass());
  }

  /**
   * Get order details.
   *
   * @param orderId Order ID
   * @return Order response
   */
  @GetMapping("/{orderId}")
  public ResponseEntity<OrderResponse> getOrderDetail(@PathVariable Long orderId) {
    logger.debug("GET /orders/{} request received", orderId);

    Long currentUserId = getCurrentUserId();
    OrderResponse response = orderService.getOrderDetail(orderId, currentUserId);

    logger.info("Successfully returned order details for orderId: {}", orderId);

    return ResponseEntity.ok(response);
  }

  /**
   * Pay for an order.
   *
   * @param orderId Order ID
   * @return Payment response
   */
  @PostMapping("/{orderId}/action/pay")
  public ResponseEntity<PayOrderResponse> payOrder(@PathVariable Long orderId) {
    logger.debug("POST /orders/{}/action/pay request received", orderId);

    Long currentUserId = getCurrentUserId();
    PayOrderResponse response = orderService.payOrder(orderId, currentUserId);

    logger.info("Successfully completed payment for orderId: {}", orderId);

    return ResponseEntity.ok(response);
  }

  @NonNull
  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return Objects.requireNonNull((Long) authentication.getPrincipal(), "User ID must not be null");
  }
}
