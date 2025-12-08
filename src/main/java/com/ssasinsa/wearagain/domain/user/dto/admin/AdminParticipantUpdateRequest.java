package com.ssasinsa.wearagain.domain.user.dto.admin;

import jakarta.validation.constraints.PositiveOrZero;

public record AdminParticipantUpdateRequest(
        @PositiveOrZero Integer ticketBalance,
        @PositiveOrZero Integer creditBalance
) {
}
