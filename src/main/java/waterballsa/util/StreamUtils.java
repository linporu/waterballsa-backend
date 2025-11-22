package waterballsa.util;

import java.util.Comparator;
import java.util.stream.Stream;

public class StreamUtils {

  private StreamUtils() {
    // Utility class - prevent instantiation
  }

  /**
   * Filters out soft-deleted entities from a stream.
   *
   * @param stream the stream of entities
   * @param <T> the type of entity that implements SoftDeletable
   * @return filtered stream without deleted entities
   */
  public static <T extends SoftDeletable> Stream<T> filterNotDeleted(Stream<T> stream) {
    return stream.filter(entity -> !entity.isDeleted());
  }

  /**
   * Sorts entities by their order index.
   *
   * @param stream the stream of entities
   * @param <T> the type of entity that implements Orderable
   * @return sorted stream
   */
  public static <T extends Orderable> Stream<T> sortByOrder(Stream<T> stream) {
    return stream.sorted(Comparator.comparing(Orderable::getOrderIndex));
  }

  /** Interface for entities that support soft deletion. */
  public interface SoftDeletable {
    boolean isDeleted();
  }

  /** Interface for entities that have an order index. */
  public interface Orderable {
    Integer getOrderIndex();
  }
}
