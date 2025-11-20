package waterballsa.dto;

public record ErrorResponse(String message, String description) {

  public ErrorResponse(String message) {
    this(message, null);
  }
}
