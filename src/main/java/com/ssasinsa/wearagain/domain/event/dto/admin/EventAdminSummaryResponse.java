package com.ssasinsa.wearagain.domain.event.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "관리자용 행사 요약 정보")
public record EventAdminSummaryResponse(
        @Schema(description = "행사 ID", example = "101")
        Long eventId,

        @Schema(description = "행사 제목", example = "지속가능 패션 워크숍")
        String title,

        @Schema(description = "행사 상태", example = "OPEN")
        String status,

        @Schema(description = "행사 시작일", example = "2025-11-10")
        LocalDate startDate,

        @Schema(description = "행사 종료일", example = "2025-11-30")
        LocalDate endDate,

        @Schema(description = "행사 위치", example = "서울시 마포구 연남동 223-14 2F")
        String location,

        @Schema(description = "총 수용 인원(옵션에 명시된 capacity 합)", example = "120")
        Long totalCapacity,

        @Schema(description = "현재 신청 인원", example = "87")
        Long appliedCount,

        @Schema(description = "잔여 인원", example = "33")
        Long remainingCount,

        @Schema(description = "행사 운영 담당자 이름", example = "운영자")
        String organizerName,

        @Schema(description = "행사 운영 담당자 이메일", example = "admin@wearagain.kr")
        String organizerContact,

        @Schema(description = "행사 담당 관리자 ID", example = "11")
        Long organizerAdminId,

        @Schema(description = "행사 담당 관리자 이메일", example = "admin@wearagain.kr")
        String organizerAdminEmail,

        @Schema(description = "행사 담당 관리자 이름", example = "홍길동")
        String organizerAdminName
) {
}
