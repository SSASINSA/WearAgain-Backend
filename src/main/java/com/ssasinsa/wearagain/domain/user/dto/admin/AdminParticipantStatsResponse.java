package com.ssasinsa.wearagain.domain.user.dto.admin;

public record AdminParticipantStatsResponse(
        long totalParticipants,
        long totalTickets,
        long totalCredits
) {
    public static AdminParticipantStatsResponse empty() {
        return new AdminParticipantStatsResponse(0, 0, 0);
    }
}
