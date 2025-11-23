package com.ssasinsa.wearagain.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스토어 상품 목록 응답")
public record StoreItemListResponse(
        @Schema(description = "상품 목록")
        List<StoreItemSummaryResponse> items,

        @Schema(description = "페이지 번호(0부터 시작)", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "10")
        int size,

        @Schema(description = "총 데이터 건수", example = "125")
        long totalElements,

        @Schema(description = "총 페이지 수", example = "13")
        int totalPages,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
}
