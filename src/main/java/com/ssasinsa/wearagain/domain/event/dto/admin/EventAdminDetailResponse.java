package com.ssasinsa.wearagain.domain.event.dto.admin;

import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "관리자용 행사 상세 응답")
public record EventAdminDetailResponse(
        @Schema(description = "행사 ID", example = "101")
        Long eventId,

        @Schema(description = "행사 제목", example = "지구환경 패션 쇼케이스")
        String title,

        @Schema(description = "행사 상세 설명", example = "지속가능한 소재로 만든 리폼 의상을 선보입니다.")
        String description,

        @Schema(description = "이용 방법 안내", example = "준비물은 개인 텀블러를 지참해주세요.")
        String usageGuide,

        @Schema(description = "주의 사항", example = "화재 예방을 위해 지정된 구역에서만 작업해주세요.")
        String precautions,

        @Schema(description = "행사 위치", example = "서울 성수동 123-4 2F")
        String location,

        @Schema(description = "주최자 이름", example = "운영자")
        String organizerName,

        @Schema(description = "주최자 이메일", example = "admin@wearagain.kr")
        String organizerContact,

        @Schema(description = "행사 담당 관리자 ID", example = "11")
        Long organizerAdminId,

        @Schema(description = "행사 담당 관리자 이메일", example = "admin@wearagain.kr")
        String organizerAdminEmail,

        @Schema(description = "행사 담당 관리자 이름", example = "운영자")
        String organizerAdminName,

        @Schema(description = "행사 시작일", example = "2025-11-10")
        LocalDate startDate,

        @Schema(description = "행사 종료일", example = "2025-11-30")
        LocalDate endDate,

        @Schema(description = "행사 상태", example = "OPEN")
        EventStatus status,

        @Schema(description = "총 수용 인원", example = "120")
        Long totalCapacity,

        @Schema(description = "현재 신청 인원", example = "87")
        Long appliedCount,

        @Schema(description = "잔여 인원", example = "33")
        Long remainingCount,

        @Schema(description = "스태프 코드", example = "023941")
        String staffCode,

        @Schema(description = "스태프 코드 발급 시각(UTC)", example = "2025-02-01T10:15:20Z")
        OffsetDateTime staffCodeIssuedAt,

        @Schema(description = "생성 시각(UTC)", example = "2025-10-21T11:20:05Z")
        OffsetDateTime createdAt,

        @Schema(description = "수정 시각(UTC)", example = "2025-11-01T09:00:00Z")
        OffsetDateTime updatedAt,

        @Schema(description = "이미지 목록")
        List<EventAdminImageResponse> images,

        @Schema(description = "옵션 트리")
        List<EventAdminOptionResponse> options,

        @Schema(description = "신청 목록")
        List<EventAdminApplicationResponse> applications,

        @Schema(description = "임팩트 분석 정보")
        EventImpactAnalyticsResponse impactAnalytics
) {

    @Schema(description = "행사 이미지 정보")
    public record EventAdminImageResponse(
            @Schema(description = "이미지 ID", example = "1001")
            Long imageId,

            @Schema(description = "이미지 URL", example = "https://cdn.wearagain.kr/events/123/main.jpg")
            String url,

            @Schema(description = "대체텍스트", example = "행사 대표 이미지")
            String altText,

            @Schema(description = "정렬 순서 (1부터 시작)", example = "1")
            int displayOrder
    ) {
    }

    @Schema(description = "행사 옵션 정보(신청 현황 포함)")
    public record EventAdminOptionResponse(
            @Schema(description = "옵션 ID", example = "2001")
            Long optionId,

            @Schema(description = "옵션 이름", example = "11월 15일")
            String name,

            @Schema(description = "정렬 순서", example = "1")
            int displayOrder,

            @Schema(description = "수용 인원(null이면 제한 없음)", example = "30")
            Integer capacity,

            @Schema(description = "현재 신청 인원", example = "25")
            Integer appliedCount,

            @Schema(description = "잔여 인원", example = "5")
            Integer remainingCount,

            @Schema(description = "하위 옵션 목록")
            List<EventAdminOptionResponse> children
    ) {
    }

    @Schema(description = "행사 신청 요약 정보")
    public record EventAdminApplicationResponse(
            @Schema(description = "신청 ID", example = "5001")
            Long applicationId,

            @Schema(description = "신청자 이메일", example = "user@wearagain.kr")
            String email,

            @Schema(description = "신청자 이름", example = "신청자")
            String displayName,

            @Schema(description = "선택한 옵션 ID", example = "2003")
            Long optionId,

            @Schema(description = "신청 상태", example = "DRAFT")
            String status,

            @Schema(description = "신청 시각(UTC)", example = "2025-11-12T04:00:00Z")
            OffsetDateTime appliedAt,

            @Schema(description = "반려/취소 사유", example = "인원 초과")
            String reason
    ) {
    }

    @Schema(description = "임팩트 분석 요약")
    public record EventImpactAnalyticsResponse(
            @Schema(description = "집계값 제공 여부", example = "true")
            boolean available,

            @Schema(description = "CO₂ 절감량 (kg)", example = "12.345")
            BigDecimal co2Saved,

            @Schema(description = "물 절감량 (L)", example = "98.100")
            BigDecimal waterSaved,

            @Schema(description = "에너지 절감량 (kWh)", example = "45.200")
            BigDecimal energySaved,

            @Schema(description = "집계 상태 메시지", example = "행사 종료 후 집계 예정입니다.")
            String message
    ) {

        public static EventImpactAnalyticsResponse pending(String message) {
            return new EventImpactAnalyticsResponse(false, null, null, null, message);
        }

        public static EventImpactAnalyticsResponse completed(
                BigDecimal co2Saved,
                BigDecimal waterSaved,
                BigDecimal energySaved
        ) {
            return new EventImpactAnalyticsResponse(true, co2Saved, waterSaved, energySaved, null);
        }
    }
}
