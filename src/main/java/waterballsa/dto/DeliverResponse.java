package waterballsa.dto;

public record DeliverResponse(
    String message, Integer experienceGained, Integer totalExperience, Integer currentLevel) {}
