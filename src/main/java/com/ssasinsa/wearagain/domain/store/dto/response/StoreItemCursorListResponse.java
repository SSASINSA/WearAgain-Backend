package com.ssasinsa.wearagain.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스토어 상품 커서 목록 응답")
public record StoreItemCursorListResponse(
        @Schema(description = "상품 목록")
        List<StoreItemSummaryResponse> items,

        @Schema(description = "다음 커서", example = "MjAyNS0wMi0xMVQwMzoxMjowMFo6MTAx")
        String nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
}
