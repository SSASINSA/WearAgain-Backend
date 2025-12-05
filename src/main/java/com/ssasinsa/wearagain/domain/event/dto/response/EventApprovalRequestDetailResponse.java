package com.ssasinsa.wearagain.domain.event.dto.response;

import com.ssasinsa.wearagain.domain.event.entity.EventApprovalRequest;
import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

@Schema(description = "이벤트 승인 요청 상세 응답")
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

        @Schema(description = "이벤트 정보")
        EventInfo event,

        @Schema(description = "이벤트 옵션 트리(최대 depth 3)")
        List<OptionInfo> options
) {
    @Schema(description = "관리자 정보")
    public record AdminInfo(
            @Schema(description = "관리자 이름", example = "이름")
            String name,

            @Schema(description = "관리자 이메일", example = "admin@example.com")
            String email
    ) {
    }

    @Schema(description = "이벤트 정보")
    public record EventInfo(
            @Schema(description = "이벤트 ID", example = "101")
            Long eventId,

            @Schema(description = "이벤트 제목", example = "지구환기 세션 워크숍")
            String title,

            @Schema(description = "이벤트 상세 설명", example = "아이들과 함께하는 리폼 클래스")
            String description,

            @Schema(description = "이벤트 장소", example = "서울시 마포구 어딘가 223-14 2F")
            String location,

            @Schema(description = "이벤트 시작일", example = "2025-11-10")
            LocalDate startDate,

            @Schema(description = "이벤트 종료일", example = "2025-11-30")
            LocalDate endDate,

            @Schema(description = "이벤트 상태", example = "DRAFT")
            String status
    ) {
    }

    @Schema(description = "이벤트 옵션 정보")
    public record OptionInfo(
            @Schema(description = "옵션 ID", example = "2001")
            Long optionId,

            @Schema(description = "옵션 이름", example = "오전 세션")
            String name,

            @Schema(description = "정렬 순서(1부터)", example = "1")
            int displayOrder,

            @Schema(description = "정원(null이면 무제한)", example = "30")
            Integer capacity,

            @Schema(description = "하위 옵션 목록")
            List<OptionInfo> children
    ) {
    }

    public static EventApprovalRequestDetailResponse from(EventApprovalRequest request) {
        AdminInfo requestingAdmin = new AdminInfo(
                request.getRequestingAdmin().getName(),
                request.getRequestingAdmin().getEmail()
        );

        AdminInfo processedByAdmin = request.getProcessedByAdmin() != null
                ? new AdminInfo(
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

        List<OptionInfo> options = request.getEvent().getOptions()
                .stream()
                .filter(option -> option.getParentOption() == null)
                .sorted(Comparator.comparingInt(EventOption::getDisplayOrder))
                .map(EventApprovalRequestDetailResponse::mapOption)
                .toList();

        return new EventApprovalRequestDetailResponse(
                request.getId(),
                toOffset(request.getCreatedAt()),
                toOffset(request.getProcessedAt()),
                requestingAdmin,
                processedByAdmin,
                eventInfo,
                options
        );
    }

    private static OptionInfo mapOption(EventOption option) {
        List<OptionInfo> children = option.getChildOptions()
                .stream()
                .sorted(Comparator.comparingInt(EventOption::getDisplayOrder))
                .map(EventApprovalRequestDetailResponse::mapOption)
                .toList();
        return new OptionInfo(
                option.getId(),
                option.getName(),
                option.getDisplayOrder(),
                option.getCapacity(),
                children
        );
    }

    private static OffsetDateTime toOffset(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atOffset(ZoneOffset.UTC);
    }
}
