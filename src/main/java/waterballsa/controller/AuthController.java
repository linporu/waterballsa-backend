package waterballsa.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import waterballsa.dto.LoginRequest;
import waterballsa.dto.LoginResponse;
import waterballsa.dto.RegisterRequest;
import waterballsa.dto.RegisterResponse;
import waterballsa.exception.InvalidCredentialsException;
import waterballsa.service.AuthService;
import waterballsa.service.RateLimitService;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;
  private final RateLimitService rateLimitService;

  public AuthController(AuthService authService, RateLimitService rateLimitService) {
    this.authService = authService;
    this.rateLimitService = rateLimitService;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    logger.debug("Register request received for username: {}", request.username());

    RegisterResponse response = authService.register(request);

    logger.info("User registration successful: {}", response.userId());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    String ipAddress = getClientIp(httpRequest);
    logger.debug(
        "Login request received for username: {} from IP: {}", request.username(), ipAddress);

    // Check rate limit
    if (!rateLimitService.tryConsume(ipAddress)) {
      logger.warn("Rate limit exceeded for IP: {}", ipAddress);
      throw new InvalidCredentialsException();
    }

    try {
      LoginResponse response = authService.login(request);

      // Reset rate limit on successful login
      rateLimitService.reset(ipAddress);

      logger.info("User login successful for username: {}", request.username());

      return ResponseEntity.ok(response);
    } catch (InvalidCredentialsException e) {
      // Re-throw the exception to be handled by GlobalExceptionHandler
      // Rate limit remains in effect for failed attempts
      throw e;
    }
  }

  /**
   * Get client IP address from HTTP request
   *
   * @param request HTTP servlet request
   * @return client IP address
   */
  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
