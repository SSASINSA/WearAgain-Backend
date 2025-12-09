package com.ssasinsa.wearagain.domain.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "관리자 행사 생성 응답")
public record EventCreateResponse(
        @Schema(description = "생성된 행사 ID", example = "100")
        Long eventId,

        @Schema(description = "행사 제목", example = "지속가능 패션 워크숍")
        String title,

        @Schema(description = "행사 상세 설명", example = "웨어어게인과 함께하는 리폼 클래스")
        String description,

        @Schema(description = "이용 방법 안내", example = "준비물은 개인 텀블러를 지참해주세요.")
        String usageGuide,

        @Schema(description = "주의 사항", example = "화재 예방을 위해 지정된 구역에서만 작업해주세요.")
        String precautions,

        @Schema(description = "행사 위치", example = "서울시 마포구 연남동 223-14 2F")
        String location,

        @Schema(description = "주최자 이름", example = "운영자")
        String organizerName,

        @Schema(description = "주최자 이메일", example = "admin@wearagain.kr")
        String organizerContact,

        @Schema(description = "행사 담당 관리자 ID", example = "11")
        Long organizerAdminId,

        @Schema(description = "행사 담당 관리자 이메일", example = "admin@wearagain.kr")
        String organizerAdminEmail,

        @Schema(description = "행사 담당 관리자 이름", example = "홍길동")
        String organizerAdminName,

        @Schema(description = "행사 시작일", example = "2025-11-10")
        LocalDate startDate,

        @Schema(description = "행사 종료일", example = "2025-11-30")
        LocalDate endDate,

        @Schema(description = "행사 상태", example = "DRAFT")
        String status,

        @Schema(description = "행사 옵션 최대 깊이", example = "2")
        int optionDepth,

        @Schema(description = "행사 이미지 목록")
        List<EventCreateImageResponse> images,

        @Schema(description = "행사 옵션 트리")
        List<EventCreateOptionResponse> options,

        @Schema(description = "생성 시각(UTC)", example = "2025-10-21T11:20:05Z")
        OffsetDateTime createdAt
) {

    @Schema(description = "행사 이미지 응답")
    public record EventCreateImageResponse(
            @Schema(description = "이미지 ID", example = "1001")
            Long eventImageId,

            @Schema(description = "이미지 URL", example = "https://cdn.wearagain.kr/events/123/main.jpg")
            String url,

            @Schema(description = "대체 텍스트", example = "행사 대표 이미지")
            String altText,

            @Schema(description = "정렬 순서 (1부터 시작)", example = "1")
            int displayOrder
    ) {
    }

    @Schema(description = "행사 옵션 응답")
    public record EventCreateOptionResponse(
            @Schema(description = "옵션 ID", example = "2001")
            Long eventOptionId,

            @Schema(description = "옵션 이름", example = "11월 15일")
            String name,

            @Schema(description = "정렬 순서 (1부터 시작)", example = "1")
            int displayOrder,

            @Schema(description = "수용 인원(필요 시)", example = "10")
            Integer capacity,

            @Schema(description = "하위 옵션 리스트")
            List<EventCreateOptionResponse> children
    ) {
    }
}
