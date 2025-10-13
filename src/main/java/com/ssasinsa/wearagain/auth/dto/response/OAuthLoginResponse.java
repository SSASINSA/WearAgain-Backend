package com.ssasinsa.wearagain.auth.dto.response;

import com.ssasinsa.wearagain.auth.domain.User;
import com.ssasinsa.wearagain.auth.infrastructure.jwt.JwtToken;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record OAuthLoginResponse(
        UUID userId,
        String email,
        String displayName,
        String profileImageUrl,
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn
) {

    public static OAuthLoginResponse of(User user, JwtToken accessToken, JwtToken refreshToken) {
        return new OAuthLoginResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getProfileImageUrl(),
                accessToken.value(),
                remainingSeconds(accessToken.expiresAt()),
                refreshToken.value(),
                remainingSeconds(refreshToken.expiresAt())
        );
    }

    private static long remainingSeconds(Instant expiresAt) {
        long seconds = Duration.between(Instant.now(), expiresAt).toSeconds();
        return Math.max(0, seconds);
    }
}
