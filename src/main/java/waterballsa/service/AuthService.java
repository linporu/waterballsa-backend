package waterballsa.service;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import waterballsa.dto.LoginRequest;
import waterballsa.dto.LoginResponse;
import waterballsa.dto.LogoutResponse;
import waterballsa.dto.RegisterRequest;
import waterballsa.dto.RegisterResponse;
import waterballsa.dto.UserInfo;
import waterballsa.entity.AccessTokenEntity;
import waterballsa.entity.UserEntity;
import waterballsa.exception.DuplicateUsernameException;
import waterballsa.exception.InvalidCredentialsException;
import waterballsa.exception.UnauthorizedException;
import waterballsa.repository.AccessTokenRepository;
import waterballsa.repository.UserRepository;
import waterballsa.util.JwtUtil;

@Service
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final AccessTokenRepository accessTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public AuthService(
      UserRepository userRepository,
      AccessTokenRepository accessTokenRepository,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.accessTokenRepository = accessTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  @Transactional
  public RegisterResponse register(RegisterRequest request) {
    logger.debug("Attempting to register user: {}", request.username());

    // Check if username already exists (excluding soft-deleted users)
    if (userRepository.existsByUsernameAndDeletedAtIsNull(request.username())) {
      logger.warn("Registration failed: username already exists: {}", request.username());
      throw new DuplicateUsernameException(request.username());
    }

    // Hash the password
    String passwordHash = passwordEncoder.encode(request.password());

    // Create and save user
    UserEntity user = new UserEntity(request.username(), passwordHash);
    UserEntity savedUser = userRepository.save(user);

    logger.info("User registered successfully with ID: {}", savedUser.getId());

    return new RegisterResponse("Registration successful", savedUser.getId());
  }

  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest request) {
    logger.debug("Attempting to login user: {}", request.username());

    // Find user by username (excluding soft-deleted users)
    UserEntity user =
        userRepository
            .findByUsernameAndDeletedAtIsNull(request.username())
            .orElseThrow(
                () -> {
                  logger.warn("Login failed: user not found: {}", request.username());
                  return new InvalidCredentialsException();
                });

    // Verify password
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      logger.warn("Login failed: invalid password for user: {}", request.username());
      throw new InvalidCredentialsException();
    }

    // Generate JWT token
    String accessToken = jwtUtil.generateToken(user);

    // Create user info
    UserInfo userInfo = new UserInfo(user.getId(), user.getUsername(), user.getExperiencePoints());

    logger.info("User login successful: {}", user.getId());

    return new LoginResponse(accessToken, userInfo);
  }

  @Transactional
  public LogoutResponse logout(String token) {
    logger.debug("Attempting to logout user with token");

    // Validate token
    if (!jwtUtil.validateToken(token)) {
      logger.warn("Logout failed: invalid token");
      throw new UnauthorizedException();
    }

    // Extract JTI and expiration from token
    String jti = jwtUtil.getJtiFromToken(token);
    LocalDateTime expiresAt = jwtUtil.getExpirationFromToken(token);
    Long userId = jwtUtil.getUserIdFromToken(token);

    // Check if token is already invalidated
    if (accessTokenRepository.existsByTokenJti(jti)) {
      logger.warn("Logout failed: token already invalidated");
      throw new UnauthorizedException();
    }

    // Add token to blacklist
    AccessTokenEntity accessToken = new AccessTokenEntity(jti, userId, expiresAt);
    accessTokenRepository.save(accessToken);

    logger.info("User logout successful: userId={}", userId);

    return new LogoutResponse("Logout successful");
  }
}
