package waterballsa.exception;

public class MissionNotFoundException extends RuntimeException {
  public MissionNotFoundException(String message) {
    super(message);
  }

  public MissionNotFoundException(Long missionId) {
    super("Mission not found: " + missionId);
  }
}
