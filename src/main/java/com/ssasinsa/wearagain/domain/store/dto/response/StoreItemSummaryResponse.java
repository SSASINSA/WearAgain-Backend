package com.ssasinsa.wearagain.domain.store.dto.response;

import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "스토어 상품 요약 응답")
public record StoreItemSummaryResponse(
        @Schema(description = "상품 ID", example = "1")
        Long id,

        @Schema(description = "상품명", example = "에코백")
        String name,

        @Schema(description = "카테고리", example = "bag")
        String category,

        @Schema(description = "판매가", example = "12000")
        Integer price,

        @Schema(description = "재고 수량", example = "100")
        Integer stock,

        @Schema(description = "상품 상태", example = "ACTIVE")
        StoreItemStatus status,

        @Schema(description = "생성 시각 (UTC)", example = "2025-11-23T12:00:00Z")
        OffsetDateTime createdAt,

        @Schema(description = "수정 시각 (UTC)", example = "2025-11-23T12:00:00Z")
        OffsetDateTime updatedAt
) {
}
