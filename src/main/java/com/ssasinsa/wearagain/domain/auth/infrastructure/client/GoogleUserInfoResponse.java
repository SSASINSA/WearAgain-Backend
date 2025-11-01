package com.ssasinsa.wearagain.domain.auth.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfoResponse(
        @JsonProperty("sub")
        String id,
        String email,
        @JsonProperty("verified_email")
        boolean verifiedEmail,
        String name,
        @JsonProperty("picture")
        String pictureUrl
) {
}
