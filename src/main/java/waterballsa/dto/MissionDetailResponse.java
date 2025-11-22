package waterballsa.dto;

import java.util.List;

public record MissionDetailResponse(
    Long id,
    Long chapterId,
    Long journeyId,
    String type,
    String title,
    String description,
    String accessLevel,
    Long createdAt,
    String videoLength,
    MissionRewardDTO reward,
    List<MissionContentDTO> content) {}
