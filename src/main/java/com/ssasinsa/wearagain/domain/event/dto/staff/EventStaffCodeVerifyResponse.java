package com.ssasinsa.wearagain.domain.event.dto.staff;

import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "스태프 코드 유효성 검사 응답")
public record EventStaffCodeVerifyResponse(
        @Schema(description = "코드 유효 여부", example = "true")
        boolean valid,

        @Schema(description = "행사 정보", nullable = true)
        EventSummary event,

        @Schema(description = "오류 코드", example = "E1026", nullable = true)
        String errorCode,

        @Schema(description = "오류 메시지", example = "유효하지 않은 스태프 코드입니다.", nullable = true)
        String message
) {

    public static EventStaffCodeVerifyResponse valid(EventSummary event) {
        return new EventStaffCodeVerifyResponse(true, event, null, null);
    }

    public static EventStaffCodeVerifyResponse invalid(EventErrorCode errorCode) {
        return new EventStaffCodeVerifyResponse(false, null, errorCode.getCode(), errorCode.getMessage());
    }

    @Schema(description = "스태프 코드로 확인한 행사 정보")
    public record EventSummary(
            @Schema(description = "행사 ID", example = "123")
            Long eventId,

            @Schema(description = "행사 제목", example = "업사이클링 체험전")
            String title,

            @Schema(description = "행사 상태", example = "PUBLISHED")
            String status,

            @Schema(description = "행사 시작일", example = "2025-03-01")
            LocalDate startDate,

            @Schema(description = "행사 종료일", example = "2025-03-02")
            LocalDate endDate,

            @Schema(description = "행사 장소", example = "서울 성수동 123-4")
            String location,

            @Schema(description = "이용 안내", example = "입장 시 QR 확인")
            String usageGuide,

            @Schema(description = "유의 사항", example = "음식물 반입 금지")
            String precautions,

            @Schema(description = "스태프 코드 발급 시각(UTC)", example = "2025-02-25T01:20:00Z")
            OffsetDateTime staffCodeIssuedAt,

            @Schema(description = "담당 관리자 이름", example = "홍길동")
            String organizerName
    ) {
        public static EventSummary from(Event event, OffsetDateTime issuedAt) {
            return new EventSummary(
                    event.getId(),
                    event.getTitle(),
                    event.getStatus().name(),
                    event.getStartDate(),
                    event.getEndDate(),
                    event.getLocation(),
                    event.getUsageGuide(),
                    event.getPrecautions(),
                    issuedAt,
                    event.getOrganizerAdmin() == null ? null : event.getOrganizerAdmin().getName()
            );
        }
    }
}
