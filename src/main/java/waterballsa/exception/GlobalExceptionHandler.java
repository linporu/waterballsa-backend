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
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("使用者名稱已存在"));
  }

  @ExceptionHandler(InvalidInputException.class)
  public ResponseEntity<ErrorResponse> handleInvalidInput(InvalidInputException ex) {
    logger.warn("Invalid input error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("輸入資料格式錯誤"));
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
    logger.warn("Invalid credentials error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("帳號或密碼錯誤"));
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
    logger.warn("Unauthorized error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("登入資料已過期"));
  }

  @ExceptionHandler(JourneyNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleJourneyNotFound(JourneyNotFoundException ex) {
    logger.warn("Journey not found error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("查無此旅程"));
  }

  @ExceptionHandler(MissionNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleMissionNotFound(MissionNotFoundException ex) {
    logger.warn("Mission not found error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("查無此任務"));
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
    logger.warn("Forbidden error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("禁止訪問"));
  }

  @ExceptionHandler(ProgressAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleProgressAccessDenied(
      ProgressAccessDeniedException ex) {
    logger.warn("Progress access denied error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("無法存取其他使用者的進度"));
  }

  @ExceptionHandler(UnsupportedMissionTypeException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedMissionType(
      UnsupportedMissionTypeException ex) {
    logger.warn("Unsupported mission type error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("此任務類型不支援進度追蹤"));
  }

  @ExceptionHandler(InvalidWatchPositionException.class)
  public ResponseEntity<ErrorResponse> handleInvalidWatchPosition(
      InvalidWatchPositionException ex) {
    logger.warn("Invalid watch position error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("觀看位置不可為負數"));
  }

  @ExceptionHandler(MissionNotCompletedException.class)
  public ResponseEntity<ErrorResponse> handleMissionNotCompleted(MissionNotCompletedException ex) {
    logger.warn("Mission not completed error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("影片任務必須先完成觀看才能交付"));
  }

  @ExceptionHandler(MissionAlreadyDeliveredException.class)
  public ResponseEntity<ErrorResponse> handleMissionAlreadyDelivered(
      MissionAlreadyDeliveredException ex) {
    logger.warn("Mission already delivered error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("此任務已經交付過了"));
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

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("資料驗證失敗，請檢查輸入內容"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    logger.error("Unexpected error occurred: ", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("系統發生錯誤，請稍後再試"));
  }
}
