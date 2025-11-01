package com.ssasinsa.wearagain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AppleOAuthLoginRequest(
        @NotBlank(message = "인가 코드를 입력해주세요.")
        @JsonProperty("code")
        String code,

        @NotBlank(message = "ID 토큰을 입력해주세요.")
        @JsonProperty("id_token")
        String idToken
) {
}
