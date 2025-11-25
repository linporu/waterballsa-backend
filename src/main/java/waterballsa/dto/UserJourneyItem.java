package waterballsa.dto;

public record UserJourneyItem(
    Long journeyId,
    String journeyTitle,
    String journeySlug,
    String coverImageUrl,
    String teacherName,
    Long purchasedAt,
    String orderNumber) {}
