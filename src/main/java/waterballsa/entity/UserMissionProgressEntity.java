package waterballsa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_mission_progress")
public class UserMissionProgressEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mission_id", nullable = false)
  private MissionEntity mission;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", nullable = false, columnDefinition = "progress_status")
  private ProgressStatusEntity status;

  @Column(name = "watch_position_seconds", nullable = false)
  private Integer watchPositionSeconds;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  protected UserMissionProgressEntity() {
    // JPA requires a no-arg constructor
  }

  public UserMissionProgressEntity(UserEntity user, MissionEntity mission) {
    this.user = user;
    this.mission = mission;
    this.status = ProgressStatusEntity.UNCOMPLETED;
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
    if (this.status == ProgressStatusEntity.UNCOMPLETED) {
      this.status = ProgressStatusEntity.COMPLETED;
    }
  }

  public void markAsDelivered() {
    this.status = ProgressStatusEntity.DELIVERED;
  }

  // Getters
  public Long getId() {
    return id;
  }

  public UserEntity getUser() {
    return user;
  }

  public MissionEntity getMission() {
    return mission;
  }

  public ProgressStatusEntity getStatus() {
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
