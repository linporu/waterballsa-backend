package waterballsa.dto;

public record UserStatusDTO(boolean hasPurchased, boolean hasUnpaidOrder, Long unpaidOrderId) {}
