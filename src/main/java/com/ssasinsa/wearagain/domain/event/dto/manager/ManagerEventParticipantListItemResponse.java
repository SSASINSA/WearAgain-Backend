package com.ssasinsa.wearagain.domain.event.dto.manager;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "매니저 참가자 목록 항목")
public record ManagerEventParticipantListItemResponse(
        @Schema(description = "신청 ID")
        Long applicationId,
        @Schema(description = "행사 ID")
        Long eventId,
        @Schema(description = "행사 제목")
        String eventTitle,
        @Schema(description = "행사 기간")
        EventPeriod eventPeriod,
        @Schema(description = "사용자 ID")
        Long userId,
        @Schema(description = "닉네임")
        String displayName,
        @Schema(description = "이메일")
        String email,
        @Schema(description = "선택 옵션 경로")
        String optionPath,
        @Schema(description = "신청 상태")
        String status,
        @Schema(description = "신청 시각")
        OffsetDateTime appliedAt,
        @Schema(description = "체크인 시각")
        OffsetDateTime checkedInAt,
        @Schema(description = "사용자 정지 여부")
        boolean suspended
) {
    @Schema(description = "행사 기간 정보")
    public record EventPeriod(
            @Schema(description = "시작일")
            LocalDate startDate,
            @Schema(description = "종료일")
            LocalDate endDate
    ) {
    }
}
