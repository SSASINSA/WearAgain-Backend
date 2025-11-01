package com.ssasinsa.wearagain.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record KakaoOAuthLoginRequest(
        @NotBlank(message = "인가 코드를 입력해주세요.")
        String authorizationCode
) {
}
