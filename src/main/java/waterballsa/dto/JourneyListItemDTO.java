package waterballsa.dto;

public record JourneyListItemDTO(
    Long id,
    String slug,
    String title,
    String description,
    String coverImageUrl,
    String teacherName) {}
