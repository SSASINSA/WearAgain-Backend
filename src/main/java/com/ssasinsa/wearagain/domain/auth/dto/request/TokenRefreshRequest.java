package com.ssasinsa.wearagain.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "사용자 토큰 재발급 요청")
public record TokenRefreshRequest(
        @Schema(description = "Refresh Token", example = "refresh-token")
        @NotBlank(message = "Refresh Token을 입력해 주세요.")
        String refreshToken
) {
}
