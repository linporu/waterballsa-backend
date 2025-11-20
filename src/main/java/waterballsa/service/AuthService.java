package waterballsa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import waterballsa.dto.RegisterRequest;
import waterballsa.dto.RegisterResponse;
import waterballsa.entity.User;
import waterballsa.exception.DuplicateUsernameException;
import waterballsa.repository.UserRepository;

@Service
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public RegisterResponse register(RegisterRequest request) {
    logger.debug("Attempting to register user: {}", request.username());

    // Check if username already exists
    if (userRepository.existsByUsername(request.username())) {
      logger.warn("Registration failed: username already exists: {}", request.username());
      throw new DuplicateUsernameException(request.username());
    }

    // Hash the password
    String passwordHash = passwordEncoder.encode(request.password());

    // Create and save user
    User user = new User(request.username(), passwordHash);
    User savedUser = userRepository.save(user);

    logger.info("User registered successfully with ID: {}", savedUser.getId());

    return new RegisterResponse("Registration successful", savedUser.getId().toString());
  }
}
