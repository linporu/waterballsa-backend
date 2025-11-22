package waterballsa.dto;

import java.util.List;

public record ChapterDTO(
    Long id, String title, Integer orderIndex, List<MissionSummaryDTO> missions) {}
