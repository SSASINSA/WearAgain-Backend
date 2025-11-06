package com.ssasinsa.wearagain.domain.event.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "관리자 행사 생성 요청")
public record EventAdminCreateRequest(
        @Schema(description = "행사 제목", example = "지속가능 패션 워크숍")
        @NotBlank
        @Size(min = 1, max = 100)
        String title,

        @Schema(description = "행사 상세 설명", example = "웨어어게인과 함께하는 리폼 클래스")
        @NotBlank
        @Size(min = 10, max = 2000)
        String description,

        @Schema(description = "이용 방법 안내", example = "준비물은 개인 텀블러를 지참해주세요.")
        @Size(max = 4000)
        String usageGuide,

        @Schema(description = "주의 사항", example = "화재 예방을 위해 지정된 구역에서만 작업해주세요.")
        @Size(max = 4000)
        String precautions,

        @Schema(description = "행사 위치", example = "서울시 마포구 연남동 223-14 2F")
        @NotBlank
        @Size(min = 1, max = 255)
        String location,

        @Schema(description = "행사 시작일", example = "2025-11-10")
        @NotNull
        LocalDate startDate,

        @Schema(description = "행사 종료일", example = "2025-11-30")
        @NotNull
        LocalDate endDate,

        @Schema(description = "행사 이미지 목록")
        @NotEmpty
        @Size(max = 10)
        List<@Valid EventAdminCreateImageRequest> images,

        @Schema(description = "행사 옵션 트리")
        List<@Valid EventAdminCreateOptionRequest> options
) {

    @Schema(description = "행사 이미지 정보")
    public record EventAdminCreateImageRequest(
            @Schema(description = "이미지 URL", example = "https://cdn.wearagain.kr/events/123/main.jpg")
            @NotBlank
            @Size(min = 1, max = 1024)
            String url,

            @Schema(description = "대체 텍스트", example = "행사 대표 이미지")
            @Size(max = 255)
            String altText,

            @Schema(description = "정렬 순서 (1부터 시작)", example = "1")
            @Positive
            int displayOrder
    ) {
    }

    @Schema(description = "행사 옵션 정보")
    public record EventAdminCreateOptionRequest(
            @Schema(description = "옵션 이름", example = "11월 15일 세션")
            @NotBlank
            @Size(min = 1, max = 100)
            String name,

            @Schema(description = "옵션 타입 라벨", example = "DATE")
            @NotBlank
            @Size(min = 1, max = 50)
            String type,

            @Schema(description = "정렬 순서 (1부터 시작)", example = "1")
            @Positive
            int displayOrder,

            @Schema(description = "수용 인원 (필요 시)", example = "10")
            Integer capacity,

            @Schema(description = "하위 옵션 목록")
            List<@Valid EventAdminCreateOptionRequest> children
    ) {
    }
}
