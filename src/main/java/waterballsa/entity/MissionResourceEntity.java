package waterballsa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "mission_resources")
public class MissionResourceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mission_id", nullable = false)
  private MissionEntity mission;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "resource_type", nullable = false, columnDefinition = "resource_type")
  private ResourceTypeEntity resourceType;

  @Column(name = "resource_url", length = 1000)
  private String resourceUrl;

  @Column(name = "resource_content", columnDefinition = "TEXT")
  private String resourceContent;

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

  protected MissionResourceEntity() {
    // JPA requires a no-arg constructor
  }

  public MissionResourceEntity(
      MissionEntity mission,
      ResourceTypeEntity resourceType,
      String resourceUrl,
      String resourceContent,
      Integer contentOrder,
      Integer durationSeconds) {
    this.mission = mission;
    this.resourceType = resourceType;
    this.resourceUrl = resourceUrl;
    this.resourceContent = resourceContent;
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

  public MissionEntity getMission() {
    return mission;
  }

  public ResourceTypeEntity getResourceType() {
    return resourceType;
  }

  public String getResourceUrl() {
    return resourceUrl;
  }

  public String getResourceContent() {
    return resourceContent;
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
