package waterballsa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "mission_contents")
public class MissionContent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mission_id", nullable = false)
  private Mission mission;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "content_type", nullable = false, columnDefinition = "content_type")
  private ContentType contentType;

  @Column(name = "content_url", nullable = false, length = 1000)
  private String contentUrl;

  @Column(name = "content_order", nullable = false)
  private Integer contentOrder;

  @Column(name = "duration_seconds")
  private Integer durationSeconds;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  protected MissionContent() {
    // JPA requires a no-arg constructor
  }

  public MissionContent(
      Mission mission,
      ContentType contentType,
      String contentUrl,
      Integer contentOrder,
      Integer durationSeconds) {
    this.mission = mission;
    this.contentType = contentType;
    this.contentUrl = contentUrl;
    this.contentOrder = contentOrder;
    this.durationSeconds = durationSeconds;
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

  public Mission getMission() {
    return mission;
  }

  public ContentType getContentType() {
    return contentType;
  }

  public String getContentUrl() {
    return contentUrl;
  }

  public Integer getContentOrder() {
    return contentOrder;
  }

  public Integer getDurationSeconds() {
    return durationSeconds;
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
