package waterballsa.dto;

public record MissionResourceDTO(
    Long id, String type, String resourceUrl, String resourceContent, Integer durationSeconds) {}
