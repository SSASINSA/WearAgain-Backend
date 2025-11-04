package com.ssasinsa.wearagain.domain.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "행사 신청 응답")
public record EventApplyResponse(
        @Schema(description = "신청 ID", example = "5001")
        Long applicationId,

        @Schema(description = "신청 상태", example = "APPLIED")
        String status
) {
}
