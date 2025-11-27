package waterballsa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "missions")
public class MissionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chapter_id", nullable = false)
  private ChapterEntity chapter;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "type", nullable = false, columnDefinition = "mission_type")
  private MissionTypeEntity type;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "access_level", nullable = false, columnDefinition = "mission_access_level")
  private MissionAccessLevelEntity accessLevel;

  @Column(name = "order_index", nullable = false)
  private Integer orderIndex;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @OneToMany(mappedBy = "mission", fetch = FetchType.LAZY)
  private List<MissionResourceEntity> resources = new ArrayList<>();

  protected MissionEntity() {
    // JPA requires a no-arg constructor
  }

  public MissionEntity(
      ChapterEntity chapter,
      String title,
      MissionTypeEntity type,
      String description,
      MissionAccessLevelEntity accessLevel,
      Integer orderIndex) {
    this.chapter = chapter;
    this.title = title;
    this.type = type;
    this.description = description;
    this.accessLevel = accessLevel;
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

  public boolean isFreePreview() {
    return this.accessLevel == MissionAccessLevelEntity.PUBLIC;
  }

  // Getters
  public Long getId() {
    return id;
  }

  public ChapterEntity getChapter() {
    return chapter;
  }

  public String getTitle() {
    return title;
  }

  public MissionTypeEntity getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  public MissionAccessLevelEntity getAccessLevel() {
    return accessLevel;
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

  public List<MissionResourceEntity> getResources() {
    return resources;
  }
}
