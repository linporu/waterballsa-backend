package waterballsa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journeys")
public class Journey {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "title", nullable = false, length = 200)
  private String title;

  @Column(name = "slug", nullable = false, unique = true, length = 200)
  private String slug;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "cover_image_url", length = 500)
  private String coverImageUrl;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "teacher_name", length = 100)
  private String teacherName;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @OneToMany(mappedBy = "journey", fetch = FetchType.LAZY)
  private List<Chapter> chapters = new ArrayList<>();

  protected Journey() {
    // JPA requires a no-arg constructor
  }

  public Journey(
      String title,
      String slug,
      String description,
      String coverImageUrl,
      BigDecimal price,
      String teacherName) {
    this.title = title;
    this.slug = slug;
    this.description = description;
    this.coverImageUrl = coverImageUrl;
    this.price = price;
    this.teacherName = teacherName;
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

  public String getTitle() {
    return title;
  }

  public String getSlug() {
    return slug;
  }

  public String getDescription() {
    return description;
  }

  public String getCoverImageUrl() {
    return coverImageUrl;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public String getTeacherName() {
    return teacherName;
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

  public List<Chapter> getChapters() {
    return chapters;
  }
}
