package waterballsa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_journeys")
public class UserJourneyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "journey_id", nullable = false)
  private Long journeyId;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "purchased_at", nullable = false)
  private LocalDateTime purchasedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  protected UserJourneyEntity() {
    // JPA requires a no-arg constructor
  }

  public UserJourneyEntity(Long userId, Long journeyId, Long orderId, LocalDateTime purchasedAt) {
    this.userId = userId;
    this.journeyId = journeyId;
    this.orderId = orderId;
    this.purchasedAt = purchasedAt;
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  // Business methods
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

  public Long getUserId() {
    return userId;
  }

  public Long getJourneyId() {
    return journeyId;
  }

  public Long getOrderId() {
    return orderId;
  }

  public LocalDateTime getPurchasedAt() {
    return purchasedAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }
}
