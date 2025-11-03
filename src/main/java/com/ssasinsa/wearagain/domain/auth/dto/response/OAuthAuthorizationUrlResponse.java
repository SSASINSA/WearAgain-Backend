package com.ssasinsa.wearagain.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth 인가 URL 응답")
public record OAuthAuthorizationUrlResponse(
        @Schema(description = "인가 URL", example = "https://accounts.google.com/o/oauth2/v2/auth?...state=xyz")
        String authorizationUrl
) {
}
