package com.ssasinsa.wearagain.domain.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "사용자 신청 내역 요약 정보")
public record EventApplicationSummaryResponse(
        @Schema(description = "신청 ID", example = "123")
        Long applicationId,

        @Schema(description = "행사 ID", example = "45")
        Long eventId,

        @Schema(description = "행사 제목", example = "업사이클링 원데이 클래스")
        String eventTitle,

        @Schema(description = "썸네일 이미지 URL", example = "https://cdn.wearagain.kr/events/45/cover.jpg")
        String thumbnailUrl,

        @Schema(description = "행사 소개", example = "'교환'과 '수선’으로 끝까지 입는 경험...")
        String description,

        @Schema(description = "행사 장소", example = "서울시 마포구 연남동 223-14 2F")
        String location,

        @Schema(description = "행사 기간")
        EventPeriod eventPeriod,

        @Schema(description = "행사 상태", example = "OPEN")
        String eventStatus
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
