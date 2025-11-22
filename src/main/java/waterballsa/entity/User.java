package waterballsa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "username", nullable = false, unique = true, length = 50)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 72)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "role", nullable = false, columnDefinition = "user_role")
  private UserRole role;

  @Column(name = "experience_points", nullable = false)
  private Integer experiencePoints;

  @Column(name = "level", nullable = false)
  private Integer level;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  protected User() {
    // JPA requires a no-arg constructor
  }

  public User(String username, String passwordHash) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.role = UserRole.STUDENT;
    this.experiencePoints = 0;
    this.level = 1;
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

  // Getters
  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public UserRole getRole() {
    return role;
  }

  public Integer getExperiencePoints() {
    return experiencePoints;
  }

  public Integer getLevel() {
    return level;
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

  public boolean isDeleted() {
    return deletedAt != null;
  }

  // Business methods
  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void addExperience(Integer points) {
    this.experiencePoints += points;
    // Level calculation will be implemented later
  }

  public enum UserRole {
    STUDENT,
    TEACHER,
    ADMIN
  }
}
