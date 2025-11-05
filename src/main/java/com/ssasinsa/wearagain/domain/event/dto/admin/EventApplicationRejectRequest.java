package com.ssasinsa.wearagain.domain.event.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "행사 신청 반려 요청")
public record EventApplicationRejectRequest(
        @Schema(description = "반려 사유", example = "예약 인원 초과로 승인 불가합니다.")
        @NotBlank
        @Size(min = 1, max = 255)
        String reason
) {
}
