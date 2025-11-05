package com.ssasinsa.wearagain.domain.event.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "행사 신청 반려 응답")
public record EventApplicationRejectResponse(
        @Schema(description = "신청 ID", example = "5001")
        Long applicationId,

        @Schema(description = "변경된 신청 상태", example = "REJECTED")
        String status
) {
}
