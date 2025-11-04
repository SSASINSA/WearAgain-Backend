package com.ssasinsa.wearagain.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Kakao ID Token 로그인 요청")
public record KakaoIdTokenLoginRequest(
        @Schema(description = "카카오 ID Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @NotBlank(message = "idToken을 입력해주세요.")
        String idToken
) {
}
