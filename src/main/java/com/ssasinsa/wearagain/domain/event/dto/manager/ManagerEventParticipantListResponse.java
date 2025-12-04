package com.ssasinsa.wearagain.domain.event.dto.manager;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "매니저 참가자 목록 응답")
public record ManagerEventParticipantListResponse(
        @Schema(description = "참가자 목록")
        List<ManagerEventParticipantListItemResponse> content,
        @Schema(description = "전체 데이터 수")
        long totalElements,
        @Schema(description = "전체 페이지 수")
        int totalPages,
        @Schema(description = "페이지 크기")
        int size,
        @Schema(description = "현재 페이지")
        int number,
        @Schema(description = "다음 페이지 여부")
        boolean hasNext,
        @Schema(description = "이전 페이지 여부")
        boolean hasPrevious,
        @Schema(description = "요약 정보")
        ManagerEventParticipantSummaryResponse summary
) {
    public static ManagerEventParticipantListResponse empty(int size) {
        return new ManagerEventParticipantListResponse(
                List.of(),
                0,
                0,
                size,
                0,
                false,
                false,
                ManagerEventParticipantSummaryResponse.empty()
        );
    }
}
