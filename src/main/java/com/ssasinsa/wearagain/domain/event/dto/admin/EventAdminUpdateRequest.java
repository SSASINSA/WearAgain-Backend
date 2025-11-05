package com.ssasinsa.wearagain.domain.event.dto.admin;

import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "관리자 행사 수정 요청")
public record EventAdminUpdateRequest(
        @Schema(description = "행사 제목", example = "지속가능 패션 워크숍 (수정)")
        @Size(min = 1, max = 100)
        String title,

        @Schema(description = "행사 상세 설명", example = "워크숍 세부 일정이 업데이트되었습니다.")
        @Size(min = 10, max = 2000)
        String description,

        @Schema(description = "행사 위치", example = "서울시 마포구 연남동 223-14 2F")
        @Size(min = 1, max = 255)
        String location,

        @Schema(description = "행사 주최자 이름", example = "웨어어게인 운영팀")
        @Size(min = 1, max = 100)
        String organizerName,

        @Schema(description = "행사 주최자 연락처", example = "02-1234-5678")
        @Size(min = 1, max = 255)
        String organizerContact,

        @Schema(description = "행사 시작일", example = "2025-11-12")
        LocalDate startDate,

        @Schema(description = "행사 종료일", example = "2025-12-01")
        LocalDate endDate,

        @Schema(description = "행사 상태", example = "OPEN")
        EventStatus status,

        @Schema(description = "행사 이미지 목록 (null이면 변경 없음, 빈 배열이면 전체 제거)")
        List<@Valid EventAdminImageRequest> images,

        @Schema(description = "행사 옵션 트리 (null이면 변경 없음, 빈 배열이면 전체 제거)")
        List<@Valid EventAdminOptionRequest> options
) {

    @Schema(description = "행사 이미지 수정 정보")
    public record EventAdminImageRequest(
            @Schema(description = "이미지 URL", example = "https://cdn.wearagain.kr/events/123/main.jpg")
            @Size(min = 1, max = 1024)
            String url,

            @Schema(description = "대체 텍스트", example = "행사 대표 이미지")
            @Size(max = 255)
            String altText,

            @Schema(description = "정렬 순서 (1부터 시작)", example = "1")
            @Positive
            Integer displayOrder
    ) {
    }

    @Schema(description = "행사 옵션 수정 정보")
    public record EventAdminOptionRequest(
            @Schema(description = "옵션 이름", example = "11월 15일")
            @Size(min = 1, max = 100)
            String name,

            @Schema(description = "옵션 타입 라벨", example = "DATE")
            @Size(min = 1, max = 50)
            String type,

            @Schema(description = "정렬 순서", example = "1")
            @Positive
            Integer displayOrder,

            @Schema(description = "수용 인원", example = "30")
            Integer capacity,

            @Schema(description = "하위 옵션 목록")
            List<@Valid EventAdminOptionRequest> children
    ) {
    }
}
