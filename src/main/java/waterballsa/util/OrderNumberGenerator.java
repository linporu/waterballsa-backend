package waterballsa.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Utility class for generating unique order numbers. */
public class OrderNumberGenerator {

  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMddHH");
  private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyz";
  private static final int RANDOM_CODE_LENGTH = 5;
  private static final SecureRandom RANDOM = new SecureRandom();

  private OrderNumberGenerator() {
    // Private constructor to prevent instantiation
  }

  /**
   * Generate order number with format: {timestamp:10 digits}{userId}{randomCode:5 chars}
   *
   * <p>Example: 20251121011117cd5 = 2025112101 (timestamp) + 11 (userId) + 17cd5 (random)
   *
   * @param userId User ID
   * @return Generated order number
   */
  public static String generate(Long userId) {
    String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    String randomCode = generateRandomCode();
    return timestamp + userId + randomCode;
  }

  /**
   * Generate random alphanumeric code.
   *
   * @return Random code string
   */
  private static String generateRandomCode() {
    StringBuilder sb = new StringBuilder(RANDOM_CODE_LENGTH);
    for (int i = 0; i < RANDOM_CODE_LENGTH; i++) {
      int index = RANDOM.nextInt(CHARACTERS.length());
      sb.append(CHARACTERS.charAt(index));
    }
    return sb.toString();
  }
}
