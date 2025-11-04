package com.ssasinsa.wearagain.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자 로그아웃 요청")
public record AdminLogoutRequest(
        @Schema(description = "관리자 Refresh Token", example = "admin-refresh-token")
        @NotBlank(message = "리프레시 토큰을 입력해 주세요.")
        String refreshToken
) {
}
