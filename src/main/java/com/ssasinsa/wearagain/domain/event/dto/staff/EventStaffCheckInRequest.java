package com.ssasinsa.wearagain.domain.event.dto.staff;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "행사 스태프 체크인 요청")
public record EventStaffCheckInRequest(
        @Schema(description = "체크인 QR 토큰", example = "3b3f6e3456d34a84b41ce8a3f7fb16b1")
        @NotBlank(message = "QR 토큰은 필수입니다.")
        String qrToken,

        @Schema(description = "스태프 코드", example = "023941")
        @NotBlank(message = "스태프 코드는 필수입니다.")
        String code
) {
}
