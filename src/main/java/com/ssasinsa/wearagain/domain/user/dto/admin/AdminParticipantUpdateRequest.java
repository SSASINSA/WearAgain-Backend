package com.ssasinsa.wearagain.domain.user.dto.admin;

import jakarta.validation.constraints.PositiveOrZero;

public record AdminParticipantUpdateRequest(
        String name,
        String avatarUrl,
        @PositiveOrZero Integer ticketBalance,
        @PositiveOrZero Integer creditBalance,
        Boolean suspended
) {
}
