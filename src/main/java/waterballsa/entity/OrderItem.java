package waterballsa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "journey_id", nullable = false)
  private Long journeyId;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal originalPrice;

  @Column(name = "discount", nullable = false, precision = 10, scale = 2)
  private BigDecimal discount;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  protected OrderItem() {
    // JPA requires a no-arg constructor
  }

  public OrderItem(
      Long journeyId, Integer quantity, BigDecimal originalPrice, BigDecimal discount) {
    this.journeyId = journeyId;
    this.quantity = quantity;
    this.originalPrice = originalPrice;
    this.discount = discount;
    this.price = originalPrice.subtract(discount);
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  // Business methods
  public void setOrder(Order order) {
    this.order = order;
  }

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }

  public boolean isDeleted() {
    return this.deletedAt != null;
  }

  // Getters
  public Long getId() {
    return id;
  }

  public Order getOrder() {
    return order;
  }

  public Long getJourneyId() {
    return journeyId;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public BigDecimal getOriginalPrice() {
    return originalPrice;
  }

  public BigDecimal getDiscount() {
    return discount;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }
}
