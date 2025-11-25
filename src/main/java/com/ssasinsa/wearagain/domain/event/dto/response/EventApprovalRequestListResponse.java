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

@Schema(description = "이벤트 승인 요청 목록 응답 DTO")
public record EventApprovalRequestListResponse(
        @Schema(description = "승인 요청 ID", example = "1")
        Long approvalRequestId,

        @Schema(description = "이벤트 정보")
        EventInfo event,

        @Schema(description = "이벤트 옵션 트리(최대 depth 3)")
        List<OptionInfo> options,

        @Schema(description = "승인 요청 관리자 정보")
        AdminInfo requestingAdmin,

        @Schema(description = "승인 요청 생성 시각", example = "2025-10-20T10:00:00Z")
        OffsetDateTime createdAt
) {
    @Schema(description = "이벤트 요약 정보")
    public record EventInfo(
            @Schema(description = "이벤트 ID", example = "101")
            Long eventId,

            @Schema(description = "이벤트 제목", example = "지구환기 세션 워크숍")
            String title,

            @Schema(description = "이벤트 장소", example = "서울시 마포구 어딘가 223-14 2F")
            String location,

            @Schema(description = "이벤트 시작일", example = "2025-11-10")
            LocalDate startDate,

            @Schema(description = "이벤트 종료일", example = "2025-11-30")
            LocalDate endDate
    ) {
    }

    @Schema(description = "관리자 정보")
    public record AdminInfo(
            @Schema(description = "관리자 이름", example = "홍길동")
            String name,

            @Schema(description = "관리자 이메일", example = "admin@example.com")
            String email
    ) {
    }

    @Schema(description = "이벤트 옵션 정보")
    public record OptionInfo(
            @Schema(description = "옵션 ID", example = "2001")
            Long optionId,

            @Schema(description = "옵션 이름", example = "오전 세션")
            String name,

            @Schema(description = "옵션 타입", example = "TIME")
            String type,

            @Schema(description = "정렬 순서(1부터)", example = "1")
            int displayOrder,

            @Schema(description = "정원(null이면 무제한)", example = "30")
            Integer capacity,

            @Schema(description = "하위 옵션 목록")
            List<OptionInfo> children
    ) {
    }

    public static EventApprovalRequestListResponse from(EventApprovalRequest request) {
        EventInfo eventInfo = new EventInfo(
                request.getEvent().getId(),
                request.getEvent().getTitle(),
                request.getEvent().getLocation(),
                request.getEvent().getStartDate(),
                request.getEvent().getEndDate()
        );

        List<OptionInfo> options = request.getEvent().getOptions()
                .stream()
                .filter(option -> option.getParentOption() == null)
                .sorted(Comparator.comparingInt(EventOption::getDisplayOrder))
                .map(EventApprovalRequestListResponse::mapOption)
                .toList();

        AdminInfo adminInfo = new AdminInfo(
                request.getRequestingAdmin().getName(),
                request.getRequestingAdmin().getEmail()
        );

        return new EventApprovalRequestListResponse(
                request.getId(),
                eventInfo,
                options,
                adminInfo,
                toOffset(request.getCreatedAt())
        );
    }

    private static OptionInfo mapOption(EventOption option) {
        List<OptionInfo> children = option.getChildOptions()
                .stream()
                .sorted(Comparator.comparingInt(EventOption::getDisplayOrder))
                .map(EventApprovalRequestListResponse::mapOption)
                .toList();
        return new OptionInfo(
                option.getId(),
                option.getName(),
                option.getType(),
                option.getDisplayOrder(),
                option.getCapacity(),
                children
        );
    }

    private static OffsetDateTime toOffset(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atOffset(ZoneOffset.UTC);
    }
}
