package com.ssasinsa.wearagain.domain.event.dto.response;

import com.ssasinsa.wearagain.domain.event.entity.EventApprovalRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Schema(description = "행사 승인 요청 목록 응답 항목")
public record EventApprovalRequestListResponse(
        @Schema(description = "승인 요청 ID", example = "1")
        Long approvalRequestId,

        @Schema(description = "행사 정보")
        EventInfo event,

        @Schema(description = "승인 요청 관리자 정보")
        AdminInfo requestingAdmin,

        @Schema(description = "승인 요청 생성 시각", example = "2025-11-21T10:00:00Z")
        OffsetDateTime createdAt
) {
    @Schema(description = "행사 요약 정보")
    public record EventInfo(
            @Schema(description = "행사 ID", example = "101")
            Long eventId,

            @Schema(description = "행사 제목", example = "지속가능 패션 워크숍")
            String title
    ) {
    }

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

    public static EventApprovalRequestListResponse from(EventApprovalRequest request) {
        EventInfo eventInfo = new EventInfo(
                request.getEvent().getId(),
                request.getEvent().getTitle()
        );

        AdminInfo adminInfo = new AdminInfo(
                request.getRequestingAdmin().getId(),
                request.getRequestingAdmin().getName(),
                request.getRequestingAdmin().getEmail()
        );

        return new EventApprovalRequestListResponse(
                request.getId(),
                eventInfo,
                adminInfo,
                toOffset(request.getCreatedAt())
        );
    }

    private static OffsetDateTime toOffset(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atOffset(ZoneOffset.UTC);
    }
}