package waterballsa.exception;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import waterballsa.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(DuplicateUsernameException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateUsername(DuplicateUsernameException ex) {
    logger.warn("Duplicate username error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse("使用者名稱已存在", "請選擇其他使用者名稱"));
  }

  @ExceptionHandler(InvalidInputException.class)
  public ResponseEntity<ErrorResponse> handleInvalidInput(InvalidInputException ex) {
    logger.warn("Invalid input error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("註冊失敗", ex.getMessage()));
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
    logger.warn("Invalid credentials error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
    logger.warn("Unauthorized error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    logger.warn("Validation errors: {}", errors);

    // Return the first validation error message for simplicity
    String firstError = errors.values().stream().findFirst().orElse("請檢查您的輸入");

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("註冊失敗", firstError));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    logger.error("Unexpected error occurred: ", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("註冊失敗", "請稍後再試"));
  }
}
