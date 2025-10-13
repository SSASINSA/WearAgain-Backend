package com.ssasinsa.wearagain.auth.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoOAuthTokenResponse(
        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("refresh_token_expires_in")
        Long refreshTokenExpiresIn,

        String scope
) {
}
