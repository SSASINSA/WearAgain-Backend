package com.ssasinsa.wearagain.domain.auth.dto.response;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.JwtToken;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 인증 토큰 응답")
public record AdminAuthTokenResponse(
        @Schema(description = "Access Token", example = "admin-access-token")
        String accessToken,

        @Schema(description = "Refresh Token", example = "admin-refresh-token")
        String refreshToken,

        @Schema(description = "Access Token 만료까지 남은 초", example = "1800")
        long expiresIn,

        @Schema(description = "관리자 역할", example = "ADMIN")
        AdminRole role,

        @Schema(description = "표시 이름", example = "관리자 홍길동")
        String displayName,

        @Schema(description = "최초 로그인 후 비밀번호 변경 필요 여부", example = "false")
        boolean mustChangePassword
) {

    public static AdminAuthTokenResponse of(
            AdminRole role,
            String displayName,
            boolean mustChangePassword,
            JwtToken accessToken,
            JwtToken refreshToken,
            long accessTokenValidityMillis
    ) {
        return new AdminAuthTokenResponse(
                accessToken.value(),
                refreshToken.value(),
                accessTokenValidityMillis / 1000,
                role,
                displayName,
                mustChangePassword
        );
    }
}
