package com.ssasinsa.wearagain.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record KakaoIdTokenLoginRequest(
        @NotBlank(message = "idToken을 입력해주세요.")
        String idToken
) {
}
