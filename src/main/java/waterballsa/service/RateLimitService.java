package waterballsa.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

  private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);

  // Rate limit: 5 attempts per 15 minutes
  private static final int MAX_ATTEMPTS = 5;
  private static final Duration REFILL_DURATION = Duration.ofMinutes(15);

  // Cache to store buckets per IP address
  private final Cache<String, Bucket> bucketCache;

  public RateLimitService() {
    this.bucketCache =
        Caffeine.newBuilder().expireAfterAccess(REFILL_DURATION).maximumSize(10000).build();
  }

  /**
   * Check if the request from the given IP is allowed
   *
   * @param ipAddress the IP address to check
   * @return true if the request is allowed, false if rate limit exceeded
   */
  public boolean tryConsume(String ipAddress) {
    Bucket bucket = resolveBucket(ipAddress);
    boolean consumed = bucket.tryConsume(1);

    if (!consumed) {
      logger.warn("Rate limit exceeded for IP: {}", ipAddress);
    }

    return consumed;
  }

  /**
   * Resolve or create a bucket for the given IP address
   *
   * @param ipAddress the IP address
   * @return the bucket for the IP address
   */
  private Bucket resolveBucket(String ipAddress) {
    return bucketCache.get(ipAddress, key -> createNewBucket());
  }

  /**
   * Create a new bucket with the configured rate limit
   *
   * @return a new bucket
   */
  private Bucket createNewBucket() {
    Bandwidth limit =
        Bandwidth.builder()
            .capacity(MAX_ATTEMPTS)
            .refillIntervally(MAX_ATTEMPTS, REFILL_DURATION)
            .build();
    return Bucket.builder().addLimit(limit).build();
  }

  /**
   * Reset the rate limit for the given IP address (useful for testing or after successful login)
   *
   * @param ipAddress the IP address to reset
   */
  public void reset(String ipAddress) {
    bucketCache.invalidate(ipAddress);
    logger.debug("Rate limit reset for IP: {}", ipAddress);
  }
}
