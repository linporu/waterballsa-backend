package waterballsa.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {

  private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);
  private static final int DB_VALIDATION_TIMEOUT_SECONDS = 2;

  private final DataSource dataSource;

  public HealthCheckService(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Map<String, Object> checkHealth() {
    Map<String, Object> health = new HashMap<>();
    long startTime = System.currentTimeMillis();

    boolean databaseHealthy = checkDatabase();
    long duration = System.currentTimeMillis() - startTime;

    health.put("status", databaseHealthy ? "UP" : "DOWN");
    health.put("database", databaseHealthy ? "UP" : "DOWN");

    // Log performance issues
    if (duration > 1000) {
      log.warn(
          "Health check slow: database={}, duration={}ms",
          databaseHealthy ? "UP" : "DOWN",
          duration);
    }

    return health;
  }

  private boolean checkDatabase() {
    try (Connection connection = dataSource.getConnection()) {
      boolean isValid = connection.isValid(DB_VALIDATION_TIMEOUT_SECONDS);

      if (!isValid) {
        // Connection obtained but validation failed (rare but possible)
        log.error(
            "Database connection validation failed: timeout={}s", DB_VALIDATION_TIMEOUT_SECONDS);
      } else {
        log.debug("Database connection healthy");
      }

      return isValid;
    } catch (SQLException e) {
      // Critical: log full error details to diagnose the issue
      log.error(
          "Database health check failed: errorCode={}, sqlState={}, message={}",
          e.getErrorCode(),
          e.getSQLState(),
          e.getMessage(),
          e);
      return false;
    }
  }
}
