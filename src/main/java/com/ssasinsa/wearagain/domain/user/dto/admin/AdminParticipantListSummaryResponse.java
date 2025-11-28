package com.ssasinsa.wearagain.domain.user.dto.admin;

public record AdminParticipantListSummaryResponse(
        long totalParticipants,
        long totalTickets,
        long totalCredits,
        long participantsChangeFromLastMonth,
        long ticketsChangeFromLastMonth,
        long creditsChangeFromLastMonth
) {

    public static AdminParticipantListSummaryResponse empty() {
        return new AdminParticipantListSummaryResponse(0, 0, 0, 0, 0, 0);
    }
}
