package com.ssasinsa.wearagain.auth.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleOAuthTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("id_token")
        String idToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        long expiresIn,

        @JsonProperty("scope")
        String scope
) {
}
