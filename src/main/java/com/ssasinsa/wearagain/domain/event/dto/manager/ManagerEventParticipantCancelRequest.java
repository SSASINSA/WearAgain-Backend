package com.ssasinsa.wearagain.domain.event.dto.manager;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "매니저 참가 신청 취소 요청")
public record ManagerEventParticipantCancelRequest(
        @Schema(description = "취소 사유", example = "중복 예약")
        @NotBlank
        @Size(max = 255)
        String reason
) {
}
