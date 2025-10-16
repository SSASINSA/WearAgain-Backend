package com.ssasinsa.wearagain.auth.dto.response;

import com.ssasinsa.wearagain.auth.infrastructure.jwt.JwtToken;
import java.time.Duration;
import java.time.Instant;

/**
 * Token 재발급 응답 DTO.
 *
 * @param accessToken           새로 발급된 Access Token
 * @param accessTokenExpiresIn  Access Token 만료까지 남은 시간(초)
 * @param refreshToken          새로 발급된 Refresh Token
 * @param refreshTokenExpiresIn Refresh Token 만료까지 남은 시간(초)
 */
public record TokenRefreshResponse(
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
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
