package com.ssasinsa.wearagain.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "어드민용 주문 목록 응답")
public record StoreAdminOrderListResponse(
        @Schema(description = "주문 목록")
        List<StoreAdminOrderSummaryResponse> orders,
        @Schema(description = "현재 페이지", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "20")
        int size,
        @Schema(description = "전체 데이터 개수", example = "154")
        long totalElements,
        @Schema(description = "전체 페이지 수", example = "8")
        int totalPages,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
}
