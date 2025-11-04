package com.ssasinsa.wearagain.domain.auth.dto.response;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.JwtToken;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import java.time.Instant;

@Schema(description = "OAuth 로그인 응답")
public record OAuthLoginResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "사용자 이메일", example = "user@wearagain.kr")
        String email,

        @Schema(description = "표시 이름", example = "웨어어게인 사용자")
        String displayName,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.wearagain.kr/users/1/profile.png")
        String profileImageUrl,

        @Schema(description = "Access Token", example = "access-token")
        String accessToken,

        @Schema(description = "Access Token 만료까지 남은 초", example = "900")
        long accessTokenExpiresIn,

        @Schema(description = "Refresh Token", example = "refresh-token")
        String refreshToken,

        @Schema(description = "Refresh Token 만료까지 남은 초", example = "1209600")
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
