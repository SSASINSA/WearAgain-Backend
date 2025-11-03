package com.ssasinsa.wearagain.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Apple OAuth 로그인 요청")
public record AppleOAuthLoginRequest(
        @Schema(description = "Apple 인가 코드", example = "c1d2e3f4")
        @NotBlank(message = "인가 코드를 입력해주세요.")
        @JsonProperty("code")
        String code,

        @Schema(description = "Apple ID Token", example = "eyJraWQiOiJBSURNIiwidHlwIjoiSldUIi...")
        @NotBlank(message = "ID 토큰을 입력해주세요.")
        @JsonProperty("id_token")
        String idToken
) {
}
