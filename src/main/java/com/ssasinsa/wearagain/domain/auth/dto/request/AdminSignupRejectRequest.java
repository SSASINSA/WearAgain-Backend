package com.ssasinsa.wearagain.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminSignupRejectRequest(
        @NotBlank(message = "거절 사유를 입력해 주세요.")
        @Size(max = 500, message = "거절 사유는 500자 이하로 입력해 주세요.")
        String reason
) {
}
