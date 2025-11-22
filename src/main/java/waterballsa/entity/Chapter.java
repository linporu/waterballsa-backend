package waterballsa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chapters")
public class Chapter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "journey_id", nullable = false)
  private Journey journey;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "order_index", nullable = false)
  private Integer orderIndex;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @OneToMany(mappedBy = "chapter", fetch = FetchType.LAZY)
  private List<Mission> missions = new ArrayList<>();

  protected Chapter() {
    // JPA requires a no-arg constructor
  }

  public Chapter(Journey journey, String title, Integer orderIndex) {
    this.journey = journey;
    this.title = title;
    this.orderIndex = orderIndex;
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
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

  public Journey getJourney() {
    return journey;
  }

  public String getTitle() {
    return title;
  }

  public Integer getOrderIndex() {
    return orderIndex;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }

  public List<Mission> getMissions() {
    return missions;
  }
}
