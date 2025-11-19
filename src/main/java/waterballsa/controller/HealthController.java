package waterballsa.controller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import waterballsa.service.HealthCheckService;

@RestController
public class HealthController {

  private static final Logger log = LoggerFactory.getLogger(HealthController.class);

  private final HealthCheckService healthCheckService;

  public HealthController(HealthCheckService healthCheckService) {
    this.healthCheckService = healthCheckService;
  }

  @GetMapping("/healthz")
  public ResponseEntity<Map<String, Object>> healthCheck() {
    long startTime = System.currentTimeMillis();

    Map<String, Object> health = healthCheckService.checkHealth();
    String status = (String) health.get("status");
    String databaseStatus = (String) health.get("database");

    long duration = System.currentTimeMillis() - startTime;

    HttpStatus httpStatus = "UP".equals(status) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

    if (httpStatus == HttpStatus.SERVICE_UNAVAILABLE) {
      // Service unhealthy: log all component statuses
      log.warn(
          "Health check FAILED: status={}, database={}, duration={}ms",
          status,
          databaseStatus,
          duration);
    } else {
      // Service healthy: simple log to avoid noise
      log.debug("Health check OK: database={}, duration={}ms", databaseStatus, duration);
    }

    return ResponseEntity.status(httpStatus).body(health);
  }
}
