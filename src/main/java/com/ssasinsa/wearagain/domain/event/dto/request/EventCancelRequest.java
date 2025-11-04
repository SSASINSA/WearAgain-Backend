package com.ssasinsa.wearagain.domain.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "행사 신청 취소 요청")
public record EventCancelRequest(
        @Size(max = 255)
        @Schema(description = "취소 사유(선택)", example = "일정이 변경되었습니다.")
        String reason
) {
}
