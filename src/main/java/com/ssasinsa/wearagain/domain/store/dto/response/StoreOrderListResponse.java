package com.ssasinsa.wearagain.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스토어 주문 커서 목록 응답")
public record StoreOrderListResponse(
        @Schema(description = "주문 목록")
        List<StoreOrderSummaryResponse> orders,
        @Schema(description = "다음 커서", example = "MjAyNS0wMi0xMVQwNDowMDowMFo6NTAx")
        String nextCursor,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
}
