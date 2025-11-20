package waterballsa.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import waterballsa.dto.RegisterRequest;
import waterballsa.dto.RegisterResponse;
import waterballsa.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    logger.debug("Register request received for username: {}", request.username());

    RegisterResponse response = authService.register(request);

    logger.info("User registration successful: {}", response.userId());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
