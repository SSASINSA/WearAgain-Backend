package com.ssasinsa.wearagain.domain.event.dto.manager;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이벤트별 참가자 요약")
public record ManagerEventParticipantEventSummaryResponse(
        @Schema(description = "행사 ID")
        Long eventId,
        @Schema(description = "행사 제목")
        String eventTitle,
        @Schema(description = "신청 수")
        long totalApplications
) {
}
