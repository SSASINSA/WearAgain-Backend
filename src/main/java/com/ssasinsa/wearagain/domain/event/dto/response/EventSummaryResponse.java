package com.ssasinsa.wearagain.domain.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "사용자 행사 목록 요약 응답")
public record EventSummaryResponse(
        @Schema(description = "행사 ID", example = "101")
        Long eventId,

        @Schema(description = "행사 제목", example = "지속가능 패션 워크숍")
        String title,

        @Schema(description = "행사 간략 설명", example = "웨어어게인과 함께하는 리폼 클래스")
        String description,

        @Schema(description = "행사 위치", example = "서울시 마포구 연남동 223-14 2F")
        String location,

        @Schema(description = "행사 시작일", example = "2025-11-10")
        LocalDate startDate,

        @Schema(description = "행사 종료일", example = "2025-11-30")
        LocalDate endDate,

        @Schema(description = "행사 상태", example = "OPEN")
        String status,

        @Schema(description = "대표 이미지 URL", example = "https://cdn.wearagain.kr/events/123/main.jpg")
        String thumbnailUrl
) {
}
