package waterballsa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_mission_progress")
public class UserMissionProgress {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mission_id", nullable = false)
  private Mission mission;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", nullable = false, columnDefinition = "progress_status")
  private ProgressStatus status;

  @Column(name = "watch_position_seconds", nullable = false)
  private Integer watchPositionSeconds;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  protected UserMissionProgress() {
    // JPA requires a no-arg constructor
  }

  public UserMissionProgress(User user, Mission mission) {
    this.user = user;
    this.mission = mission;
    this.status = ProgressStatus.UNCOMPLETED;
    this.watchPositionSeconds = 0;
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

  public void updateWatchPosition(Integer watchPositionSeconds) {
    this.watchPositionSeconds = watchPositionSeconds;
  }

  public void markAsCompleted() {
    if (this.status == ProgressStatus.UNCOMPLETED) {
      this.status = ProgressStatus.COMPLETED;
    }
  }

  public void markAsDelivered() {
    this.status = ProgressStatus.DELIVERED;
  }

  // Getters
  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public Mission getMission() {
    return mission;
  }

  public ProgressStatus getStatus() {
    return status;
  }

  public Integer getWatchPositionSeconds() {
    return watchPositionSeconds;
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
}
