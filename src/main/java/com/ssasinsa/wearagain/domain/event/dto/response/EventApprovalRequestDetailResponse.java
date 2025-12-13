package com.ssasinsa.wearagain.domain.event.dto.response;

import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventApprovalRequest;
import com.ssasinsa.wearagain.domain.event.entity.EventImage;
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

        @Schema(description = "이벤트 총 수용 인원", example = "150", nullable = true)
        Long totalCapacity,

        @Schema(description = "이벤트 옵션 최대 깊이", example = "3")
        int optionDepth,

        @Schema(description = "이벤트 이미지 목록")
        List<ImageInfo> images,

        @Schema(description = "이벤트 옵션 트리(최대 depth 3)")
        List<OptionInfo> options
) {

    @Schema(description = "관리자 정보")
    public record AdminInfo(
            @Schema(description = "관리자 이름", example = "홍길동")
            String name,

            @Schema(description = "관리자 이메일", example = "admin@example.com")
            String email
    ) {
    }

    @Schema(description = "이벤트 정보")
    public record EventInfo(
            @Schema(description = "이벤트 ID", example = "101")
            Long eventId,

            @Schema(description = "이벤트 제목", example = "지구환경체험 워크샵")
            String title,

            @Schema(description = "이벤트 상세 설명", example = "아이들과 함께하는 리폼 클래스입니다.")
            String description,

            @Schema(description = "이용 가이드", example = "준비물은 사전에 문자로 안내드립니다.", nullable = true)
            String usageGuide,

            @Schema(description = "주의 사항", example = "음식물 반입 금지", nullable = true)
            String precautions,

            @Schema(description = "이벤트 장소", example = "서울특별시 마포구 양화로 223-14 2F")
            String location,

            @Schema(description = "이벤트 시작일", example = "2025-11-10")
            LocalDate startDate,

            @Schema(description = "이벤트 종료일", example = "2025-11-30")
            LocalDate endDate,

            @Schema(description = "이벤트 상태", example = "DRAFT")
            String status,

            @Schema(description = "주최자 이름", example = "홍길동", nullable = true)
            String organizerName,

            @Schema(description = "주최자 연락처", example = "organizer@wearagain.kr", nullable = true)
            String organizerContact,

            @Schema(description = "주최 관리자 ID", example = "11", nullable = true)
            Long organizerAdminId,

            @Schema(description = "주최 관리자 이메일", example = "admin@wearagain.kr", nullable = true)
            String organizerAdminEmail,

            @Schema(description = "주최 관리자 이름", example = "홍길동", nullable = true)
            String organizerAdminName
    ) {
    }

    @Schema(description = "이벤트 이미지 정보")
    public record ImageInfo(
            @Schema(description = "이미지 ID", example = "1001")
            Long imageId,

            @Schema(description = "이미지 URL", example = "https://cdn.wearagain.kr/events/101/main.jpg")
            String url,

            @Schema(description = "이미지 대체 텍스트", example = "대표 이미지", nullable = true)
            String altText,

            @Schema(description = "노출 순서", example = "1")
            int displayOrder
    ) {
    }

    @Schema(description = "이벤트 옵션 정보")
    public record OptionInfo(
            @Schema(description = "옵션 ID", example = "2001")
            Long optionId,

            @Schema(description = "옵션 이름", example = "1일차")
            String name,

            @Schema(description = "노출 순서(1부터 시작)", example = "1")
            int displayOrder,

            @Schema(description = "수용 인원(null이면 제한 없음)", example = "30")
            Integer capacity,

            @Schema(description = "하위 옵션 목록")
            List<OptionInfo> children
    ) {
    }

    public static EventApprovalRequestDetailResponse from(EventApprovalRequest request, Long totalCapacity) {
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

        Event event = request.getEvent();
        AdminUser organizerAdmin = event.getOrganizerAdmin();

        EventInfo eventInfo = new EventInfo(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getUsageGuide(),
                event.getPrecautions(),
                event.getLocation(),
                event.getStartDate(),
                event.getEndDate(),
                event.getStatus().name(),
                organizerAdmin == null ? null : organizerAdmin.getName(),
                organizerAdmin == null ? null : organizerAdmin.getEmail(),
                organizerAdmin == null ? null : organizerAdmin.getId(),
                organizerAdmin == null ? null : organizerAdmin.getEmail(),
                organizerAdmin == null ? null : organizerAdmin.getName()
        );

        List<ImageInfo> images = event.getImages()
                .stream()
                .sorted(Comparator.comparingInt(EventImage::getDisplayOrder))
                .map(image -> new ImageInfo(
                        image.getId(),
                        image.getUrl(),
                        image.getAltText(),
                        image.getDisplayOrder()
                ))
                .toList();

        List<OptionInfo> options = event.getOptions()
                .stream()
                .filter(option -> option.getParentOption() == null)
                .sorted(Comparator.comparingInt(EventOption::getDisplayOrder))
                .map(EventApprovalRequestDetailResponse::mapOption)
                .toList();

        int depth = event.getOptionDepth() == null ? 1 : event.getOptionDepth();

        return new EventApprovalRequestDetailResponse(
                request.getId(),
                toOffset(request.getCreatedAt()),
                toOffset(request.getProcessedAt()),
                requestingAdmin,
                processedByAdmin,
                eventInfo,
                totalCapacity,
                depth,
                images,
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
