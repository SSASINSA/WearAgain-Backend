package com.ssasinsa.wearagain.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleOAuthLoginRequest(
        @NotBlank(message = "authorizationCode는 필수입니다.")
        String authorizationCode
) {
}
