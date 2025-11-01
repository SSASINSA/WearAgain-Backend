package com.ssasinsa.wearagain.auth.infrastructure.jwt;

import java.time.Instant;
import java.util.UUID;

public record JwtToken(
        String value,
        Instant expiresAt,
        UUID tokenId
) {
}
