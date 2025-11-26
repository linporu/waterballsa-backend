package waterballsa.dto;

import java.util.List;

public record JourneyDetailResponse(
    Long id,
    String slug,
    String title,
    String description,
    String coverImageUrl,
    String teacherName,
    UserStatusDTO userStatus,
    List<ChapterDTO> chapters) {}
