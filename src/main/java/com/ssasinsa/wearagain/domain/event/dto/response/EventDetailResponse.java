package com.ssasinsa.wearagain.domain.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "사용자 행사 상세 응답")
public record EventDetailResponse(
        @Schema(description = "행사 ID", example = "101")
        Long eventId,

        @Schema(description = "행사 제목", example = "지속가능 패션 워크숍")
        String title,

        @Schema(description = "행사 상세 설명", example = "웨어어게인과 함께하는 리폼 클래스")
        String description,

        @Schema(description = "행사 위치", example = "서울시 마포구 연남동 223-14 2F")
        String location,

        @Schema(description = "주최자 이름", example = "운영자")
        String organizerName,

        @Schema(description = "주최자 이메일", example = "admin@wearagain.kr")
        String organizerContact,

        @Schema(description = "행사 시작일", example = "2025-11-10")
        LocalDate startDate,

        @Schema(description = "행사 종료일", example = "2025-11-30")
        LocalDate endDate,

        @Schema(description = "행사 상태", example = "OPEN")
        String status,

        @Schema(description = "이미지 목록")
        List<EventDetailImageResponse> images,

        @Schema(description = "옵션 트리")
        List<EventDetailOptionResponse> options
) {

    @Schema(description = "행사 이미지 상세")
    public record EventDetailImageResponse(
            @Schema(description = "이미지 ID", example = "1001")
            Long imageId,

            @Schema(description = "이미지 URL", example = "https://cdn.wearagain.kr/events/123/main.jpg")
            String url,

            @Schema(description = "대체 텍스트", example = "행사 대표 이미지")
            String altText,

            @Schema(description = "정렬 순서 (1부터 시작)", example = "1")
            int displayOrder
    ) {
    }

    @Schema(description = "행사 옵션 상세")
    public record EventDetailOptionResponse(
            @Schema(description = "옵션 ID", example = "2001")
            Long optionId,

            @Schema(description = "옵션 이름", example = "11월 15일")
            String name,

            @Schema(description = "옵션 타입 라벨", example = "DATE")
            String type,

            @Schema(description = "정렬 순서 (1부터 시작)", example = "1")
            int displayOrder,

            @Schema(description = "수용 인원(선택)", example = "10")
            Integer capacity,

            @Schema(description = "현재 신청 인원", example = "7")
            Integer appliedCount,

            @Schema(description = "잔여 인원 (capacity 있을 경우에만)", example = "3")
            Integer remainingCount,

            @Schema(description = "하위 옵션 목록")
            List<EventDetailOptionResponse> children
    ) {
    }
}
