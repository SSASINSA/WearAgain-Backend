package com.ssasinsa.wearagain.domain.event.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "사용자 신청 상세 응답")
public record EventApplicationDetailResponse(
        @Schema(description = "신청 ID", example = "123")
        Long applicationId,

        @Schema(description = "행사 ID", example = "45")
        Long eventId,

        @Schema(description = "행사 제목", example = "업사이클링 원데이 클래스")
        String eventTitle,

        @Schema(description = "행사 상태", example = "OPEN")
        String eventStatus,

        @Schema(description = "행사 기간 정보")
        EventPeriod eventPeriod,

        @Schema(description = "행사 장소", example = "서울시 마포구 연남동 223-14 2F")
        String location,

        @Schema(description = "행사 설명", example = "'교환'과 '수선’으로 끝까지 입는 경험과 실천을 제공하는 지속 가능한 의생활 실험 공간")
        String description,

        @Schema(description = "이용 안내", example = "현장에는 개인 텀블러를 지참해주세요.")
        String usageGuide,

        @Schema(description = "주의 사항", example = "화재 예방을 위해 지정된 구역에서만 작업해주세요.")
        String precautions,

        @Schema(description = "신청 시 선택한 옵션 계층")
        List<OptionTrailResponse> optionTrail
) {

    @Schema(description = "행사 기간")
    public record EventPeriod(
            @Schema(description = "행사 시작일", example = "2025-02-10")
            LocalDate startDate,
            @Schema(description = "행사 종료일", example = "2025-02-11")
            LocalDate endDate
    ) {
    }

    @Schema(description = "선택한 옵션 계층 정보")
    public record OptionTrailResponse(
            @Schema(description = "옵션 ID", example = "2001")
            Long eventOptionId,
            @Schema(description = "옵션 이름", example = "11월 15일")
            String name,
            @Schema(description = "옵션 타입", example = "DATE")
            String type
    ) {
    }
}
