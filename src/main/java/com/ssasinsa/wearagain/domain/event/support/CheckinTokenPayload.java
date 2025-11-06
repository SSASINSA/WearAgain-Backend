package com.ssasinsa.wearagain.domain.event.support;

import java.time.OffsetDateTime;

public record CheckinTokenPayload(
        Long applicationId,
        String token,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt
) {
}
