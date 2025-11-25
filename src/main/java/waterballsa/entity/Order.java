package waterballsa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.lang.NonNull;

@Entity
@Table(name = "orders")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_number", nullable = false, unique = true, length = 50)
  private String orderNumber;

  @NonNull
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", nullable = false, columnDefinition = "order_status")
  private OrderStatus status;

  @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal originalPrice;

  @Column(name = "discount", nullable = false, precision = 10, scale = 2)
  private BigDecimal discount;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "paid_at")
  private LocalDateTime paidAt;

  @Column(name = "expired_at")
  private LocalDateTime expiredAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> items = new ArrayList<>();

  @SuppressWarnings("null")
  protected Order() {
    // JPA requires a no-arg constructor
  }

  public Order(
      String orderNumber, @NonNull Long userId, BigDecimal originalPrice, BigDecimal discount) {
    this.orderNumber = orderNumber;
    this.userId = userId;
    this.status = OrderStatus.UNPAID;
    this.originalPrice = originalPrice;
    this.discount = discount;
    this.price = originalPrice.subtract(discount);
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    // Set expiration to 3 days after creation for unpaid orders
    if (this.status == OrderStatus.UNPAID) {
      this.expiredAt = this.createdAt.plusDays(3);
    }
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  // Business methods
  public void addItem(OrderItem item) {
    items.add(item);
    item.setOrder(this);
  }

  public void markAsPaid() {
    this.status = OrderStatus.PAID;
    this.paidAt = LocalDateTime.now();
  }

  public boolean isPaid() {
    return this.status == OrderStatus.PAID;
  }

  public void markAsExpired() {
    this.status = OrderStatus.EXPIRED;
  }

  public boolean isExpired() {
    return this.status == OrderStatus.EXPIRED;
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

  public String getOrderNumber() {
    return orderNumber;
  }

  @NonNull
  public Long getUserId() {
    return userId;
  }

  public OrderStatus getStatus() {
    return status;
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

  public LocalDateTime getPaidAt() {
    return paidAt;
  }

  public LocalDateTime getExpiredAt() {
    return expiredAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }

  public List<OrderItem> getItems() {
    return items;
  }
}
