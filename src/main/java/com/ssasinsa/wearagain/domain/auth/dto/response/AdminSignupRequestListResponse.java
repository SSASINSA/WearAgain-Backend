package com.ssasinsa.wearagain.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "관리자 가입 요청 목록 응답")
public record AdminSignupRequestListResponse(
        @Schema(description = "요청 요약 목록")
        List<AdminSignupRequestSummaryResponse> items,
        @Schema(description = "현재 페이지 (0-base)", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "20")
        int size,
        @Schema(description = "전체 요청 수", example = "125")
        long totalElements,
        @Schema(description = "전체 페이지 수", example = "7")
        int totalPages,
        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {

    public static AdminSignupRequestListResponse of(
            List<AdminSignupRequestSummaryResponse> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        return new AdminSignupRequestListResponse(
                List.copyOf(items),
                page,
                size,
                totalElements,
                totalPages,
                hasNext
        );
    }
}

