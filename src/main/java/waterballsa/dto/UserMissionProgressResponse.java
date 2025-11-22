package waterballsa.dto;

public record UserMissionProgressResponse(
    Long missionId, String status, Integer watchPositionSeconds) {}
