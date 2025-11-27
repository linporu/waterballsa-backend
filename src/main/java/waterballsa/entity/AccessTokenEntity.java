package waterballsa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_tokens")
public class AccessTokenEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "token_jti", nullable = false, unique = true, length = 64)
  private String tokenJti;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "invalidated_at", nullable = false)
  private LocalDateTime invalidatedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  protected AccessTokenEntity() {
    // JPA requires a no-arg constructor
  }

  public AccessTokenEntity(String tokenJti, Long userId, LocalDateTime expiresAt) {
    this.tokenJti = tokenJti;
    this.userId = userId;
    this.expiresAt = expiresAt;
    this.invalidatedAt = LocalDateTime.now();
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    if (this.invalidatedAt == null) {
      this.invalidatedAt = LocalDateTime.now();
    }
  }

  // Getters
  public Long getId() {
    return id;
  }

  public String getTokenJti() {
    return tokenJti;
  }

  public Long getUserId() {
    return userId;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public LocalDateTime getInvalidatedAt() {
    return invalidatedAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }
}
