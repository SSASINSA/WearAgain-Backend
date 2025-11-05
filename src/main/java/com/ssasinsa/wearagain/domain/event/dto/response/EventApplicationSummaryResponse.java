package com.ssasinsa.wearagain.domain.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "사용자 신청 내역 요약 정보")
public record EventApplicationSummaryResponse(
        @Schema(description = "신청 ID", example = "123")
        Long applicationId,

        @Schema(description = "행사 ID", example = "45")
        Long eventId,

        @Schema(description = "행사 제목", example = "업사이클링 원데이 클래스")
        String eventTitle,

        @Schema(description = "행사 기간")
        EventPeriod eventPeriod,

        @Schema(description = "선택한 옵션 이름", example = "1일차 오후 세션")
        String optionName,

        @Schema(description = "신청 상태", example = "APPLIED")
        String status,

        @Schema(description = "신청 시각(UTC)", example = "2025-01-28T12:30:00Z")
        OffsetDateTime appliedAt,

        @Schema(description = "QR 발급 가능 여부", example = "true")
        boolean qrAvailable,

        @Schema(description = "체크인 시각(UTC)", example = "2025-02-10T09:05:12Z")
        OffsetDateTime checkedInAt
) {

    @Schema(description = "행사 기간 정보")
    public record EventPeriod(
            @Schema(description = "행사 시작일", example = "2025-02-10")
            LocalDate startDate,
            @Schema(description = "행사 종료일", example = "2025-02-11")
            LocalDate endDate
    ) {
    }
}
