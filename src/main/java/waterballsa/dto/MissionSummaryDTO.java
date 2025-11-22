package waterballsa.dto;

public record MissionSummaryDTO(
    Long id, String type, String title, String accessLevel, Integer orderIndex, String status) {}
