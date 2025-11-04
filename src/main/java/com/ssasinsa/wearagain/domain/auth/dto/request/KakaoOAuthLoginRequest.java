package com.ssasinsa.wearagain.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Kakao OAuth 로그인 요청")
public record KakaoOAuthLoginRequest(
        @Schema(description = "카카오 인가 코드", example = "J0Lx...")
        @NotBlank(message = "인가 코드를 입력해주세요.")
        String authorizationCode
) {
}
