package com.ssasinsa.wearagain.domain.user.dto.admin;

import java.time.OffsetDateTime;

public record AdminParticipantDetailResponse(
        Long participantId,
        String name,
        String email,
        String avatarUrl,
        int ticketBalance,
        int creditBalance,
        boolean suspended,
        OffsetDateTime joinedAt,
        OffsetDateTime updatedAt,
        AdminImpactSummaryResponse impact,
        AdminMascotResponse mascot,
        java.util.List<AdminRecentEventResponse> recentEvents
) {
}
