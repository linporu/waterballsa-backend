package richardlin.io.waterballsabackend.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import richardlin.io.waterballsabackend.service.HealthCheckService;

@RestController
public class HealthController {

  private final HealthCheckService healthCheckService;

  public HealthController(HealthCheckService healthCheckService) {
    this.healthCheckService = healthCheckService;
  }

  @GetMapping("/healthz")
  public ResponseEntity<Map<String, Object>> healthCheck() {
    Map<String, Object> health = healthCheckService.checkHealth();
    String status = (String) health.get("status");

    HttpStatus httpStatus = "UP".equals(status) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

    return ResponseEntity.status(httpStatus).body(health);
  }
}
