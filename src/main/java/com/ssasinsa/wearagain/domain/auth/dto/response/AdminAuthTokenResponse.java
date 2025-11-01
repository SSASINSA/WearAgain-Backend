package com.ssasinsa.wearagain.domain.auth.dto.response;

import com.ssasinsa.wearagain.domain.auth.infrastructure.jwt.JwtToken;
import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;

public record AdminAuthTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        AdminRole role,
        String displayName,
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
