package com.ssasinsa.wearagain.domain.auth.infrastructure.jwt;

import java.time.Instant;
import java.util.UUID;

public record JwtToken(
        String value,
        Instant expiresAt,
        UUID tokenId
) {
}
