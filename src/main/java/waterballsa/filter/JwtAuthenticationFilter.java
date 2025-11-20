package waterballsa.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import waterballsa.util.JwtUtil;

/**
 * JWT Authentication Filter that validates JWT tokens from Authorization header.
 *
 * <p>This filter:
 *
 * <ul>
 *   <li>Extracts Bearer token from Authorization header
 *   <li>Validates the token using JwtUtil
 *   <li>Sets authentication in SecurityContext if token is valid
 * </ul>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtUtil jwtUtil;

  public JwtAuthenticationFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader(AUTHORIZATION_HEADER);

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(BEARER_PREFIX.length());

    if (!jwtUtil.validateToken(token)) {
      logger.debug("Invalid JWT token");
      filterChain.doFilter(request, response);
      return;
    }

    try {
      Long userId = jwtUtil.getUserIdFromToken(token);
      String username = jwtUtil.getUsernameFromToken(token);

      // Create authentication object with userId as principal and username as credentials
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userId, username, null);

      // Set authentication in security context
      SecurityContextHolder.getContext().setAuthentication(authentication);

      logger.debug("Successfully authenticated user {} with token", userId);
    } catch (Exception e) {
      logger.debug("Failed to extract user info from token: {}", e.getMessage());
    }

    filterChain.doFilter(request, response);
  }
}
