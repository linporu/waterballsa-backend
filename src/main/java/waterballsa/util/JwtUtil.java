package waterballsa.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import waterballsa.entity.UserEntity;

@Component
public class JwtUtil {

  private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

  private final SecretKey secretKey;
  private final long expirationMs;

  public JwtUtil(
      @Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expirationMs) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.expirationMs = expirationMs;
  }

  /**
   * Generate JWT token for the given user
   *
   * @param user the user to generate token for
   * @return JWT token string
   */
  public String generateToken(UserEntity user) {
    Instant now = Instant.now();
    Instant expiration = now.plusMillis(expirationMs);
    String jti = UUID.randomUUID().toString();

    String token =
        Jwts.builder()
            .id(jti)
            .subject(user.getId().toString())
            .claim("username", user.getUsername())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact();

    logger.debug("Generated JWT token for user: {} with JTI: {}", user.getId(), jti);

    return token;
  }

  /**
   * Validate JWT token
   *
   * @param token JWT token to validate
   * @return true if token is valid, false otherwise
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      logger.debug("JWT validation failed: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Get user ID from JWT token
   *
   * @param token JWT token
   * @return user ID
   */
  public Long getUserIdFromToken(String token) {
    Claims claims =
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    return Long.parseLong(claims.getSubject());
  }

  /**
   * Get JWT ID (jti) from token
   *
   * @param token JWT token
   * @return JWT ID (jti)
   */
  public String getJtiFromToken(String token) {
    Claims claims =
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    return claims.getId();
  }

  /**
   * Get expiration time from JWT token
   *
   * @param token JWT token
   * @return expiration time as LocalDateTime
   */
  public LocalDateTime getExpirationFromToken(String token) {
    Claims claims =
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    return LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
  }

  /**
   * Get username from JWT token
   *
   * @param token JWT token
   * @return username
   */
  public String getUsernameFromToken(String token) {
    Claims claims =
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    return claims.get("username", String.class);
  }
}
