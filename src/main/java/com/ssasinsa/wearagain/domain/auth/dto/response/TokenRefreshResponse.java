package com.ssasinsa.wearagain.domain.auth.dto.response;

import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.JwtToken;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import java.time.Instant;

@Schema(description = "토큰 재발급 응답")
public record TokenRefreshResponse(
        @Schema(description = "새 Access Token", example = "new-access-token")
        String accessToken,

        @Schema(description = "Access Token 만료까지 남은 초", example = "900")
        long accessTokenExpiresIn,

        @Schema(description = "새 Refresh Token", example = "new-refresh-token")
        String refreshToken,

        @Schema(description = "Refresh Token 만료까지 남은 초", example = "1209600")
        long refreshTokenExpiresIn
) {

    public static TokenRefreshResponse of(JwtToken accessToken, JwtToken refreshToken) {
        return new TokenRefreshResponse(
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
