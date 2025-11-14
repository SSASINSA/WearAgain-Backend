package com.ssasinsa.wearagain.domain.ticket.support;

import java.time.OffsetDateTime;

public record TicketQrTokenPayload(
        Long userId,
        String token,
        int ticketCount,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt
) {
}
