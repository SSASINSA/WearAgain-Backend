package com.ssasinsa.wearagain.domain.event.dto.response;

import com.ssasinsa.wearagain.domain.event.entity.EventApprovalRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Schema(description = "행사 승인 요청 상세 응답")
public record EventApprovalRequestDetailResponse(
        @Schema(description = "승인 요청 ID", example = "1")
        Long approvalRequestId,

        @Schema(description = "승인 요청 생성 시각", example = "2025-11-21T10:00:00Z")
        OffsetDateTime createdAt,

        @Schema(description = "승인 처리 시각", example = "2025-11-21T10:30:00Z", nullable = true)
        OffsetDateTime processedAt,

        @Schema(description = "승인 요청 관리자 정보")
        AdminInfo requestingAdmin,

        @Schema(description = "승인 처리 관리자 정보", nullable = true)
        AdminInfo processedByAdmin,

        @Schema(description = "행사 정보")
        EventInfo event
) {
    @Schema(description = "관리자 정보")
    public record AdminInfo(
            @Schema(description = "관리자 ID", example = "1")
            Long adminId,

            @Schema(description = "관리자 이름", example = "이름")
            String name,

            @Schema(description = "관리자 이메일", example = "admin@example.com")
            String email
    ) {
    }

    @Schema(description = "행사 정보")
    public record EventInfo(
            @Schema(description = "행사 ID", example = "101")
            Long eventId,

            @Schema(description = "행사 제목", example = "지속가능 패션 워크숍")
            String title,

            @Schema(description = "행사 상세 설명", example = "웨어어게인과 함께하는 리폼 클래스")
            String description,

            @Schema(description = "행사 위치", example = "서울시 마포구 연남동 223-14 2F")
            String location,

            @Schema(description = "행사 시작일", example = "2025-11-10")
            LocalDate startDate,

            @Schema(description = "행사 종료일", example = "2025-11-30")
            LocalDate endDate,

            @Schema(description = "행사 상태", example = "DRAFT")
            String status
    ) {
    }

    public static EventApprovalRequestDetailResponse from(EventApprovalRequest request) {
        AdminInfo requestingAdmin = new AdminInfo(
                request.getRequestingAdmin().getId(),
                request.getRequestingAdmin().getName(),
                request.getRequestingAdmin().getEmail()
        );

        AdminInfo processedByAdmin = request.getProcessedByAdmin() != null
                ? new AdminInfo(
                        request.getProcessedByAdmin().getId(),
                        request.getProcessedByAdmin().getName(),
                        request.getProcessedByAdmin().getEmail()
                )
                : null;

        EventInfo eventInfo = new EventInfo(
                request.getEvent().getId(),
                request.getEvent().getTitle(),
                request.getEvent().getDescription(),
                request.getEvent().getLocation(),
                request.getEvent().getStartDate(),
                request.getEvent().getEndDate(),
                request.getEvent().getStatus().name()
        );

        return new EventApprovalRequestDetailResponse(
                request.getId(),
                toOffset(request.getCreatedAt()),
                toOffset(request.getProcessedAt()),
                requestingAdmin,
                processedByAdmin,
                eventInfo
        );
    }

    private static OffsetDateTime toOffset(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atOffset(ZoneOffset.UTC);
    }
}
