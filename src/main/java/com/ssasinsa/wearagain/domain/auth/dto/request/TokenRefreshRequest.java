package com.ssasinsa.wearagain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
        @NotBlank(message = "Refresh Token을 입력해 주세요.")
        String refreshToken
) {
}
