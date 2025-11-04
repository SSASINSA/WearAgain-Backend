package com.ssasinsa.wearagain.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 가입 거절 요청")
public record AdminSignupRejectRequest(
        @Schema(description = "거절 사유", example = "필수 서류 미비")
        @NotBlank(message = "거절 사유를 입력해 주세요.")
        @Size(max = 500, message = "거절 사유는 500자 이하로 입력해 주세요.")
        String reason
) {
}
