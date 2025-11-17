package com.ssasinsa.wearagain.domain.event.dto.staff;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "스태프 코드 유효성 검사 요청")
public record EventStaffCodeVerifyRequest(
        @Schema(description = "6자리 스태프 코드", example = "023941")
        @NotBlank
        String code
) {
}
