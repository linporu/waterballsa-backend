package waterballsa.dto;

import java.math.BigDecimal;

public record JourneyListItemDTO(
    Long id,
    String slug,
    String title,
    String description,
    String coverImageUrl,
    String teacherName,
    BigDecimal price) {}
