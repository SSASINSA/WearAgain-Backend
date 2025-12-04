package com.ssasinsa.wearagain.domain.event.dto.manager;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "참가자 목록 요약 정보")
public record ManagerEventParticipantSummaryResponse(
        @Schema(description = "총 신청 수")
        long totalApplications,
        @Schema(description = "APPLIED 수")
        long appliedCount,
        @Schema(description = "CHECKED_IN 수")
        long checkedInCount,
        @Schema(description = "CANCELLED 수")
        long cancelledCount,
        @Schema(description = "REJECTED 수")
        long rejectedCount,
        @Schema(description = "행사별 신청 요약")
        List<ManagerEventParticipantEventSummaryResponse> events
) {
    public static ManagerEventParticipantSummaryResponse empty() {
        return new ManagerEventParticipantSummaryResponse(0, 0, 0, 0, 0, List.of());
    }
}
