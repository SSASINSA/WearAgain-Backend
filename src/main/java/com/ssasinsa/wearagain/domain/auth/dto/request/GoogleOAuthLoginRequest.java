package com.ssasinsa.wearagain.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Google OAuth 로그인 요청")
public record GoogleOAuthLoginRequest(
        @Schema(description = "Google 인가 코드", example = "4/0Ad...")
        @NotBlank(message = "authorizationCode는 필수입니다.")
        String authorizationCode
) {
}
