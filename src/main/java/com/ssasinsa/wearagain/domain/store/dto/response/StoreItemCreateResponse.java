package com.ssasinsa.wearagain.domain.store.dto.response;

import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스토어 상품 등록 응답")
public record StoreItemCreateResponse(
        @Schema(description = "상품 ID", example = "1")
        Long id,

        @Schema(description = "상품명", example = "에코백")
        String name,

        @Schema(description = "상품 상태", example = "ACTIVE")
        StoreItemStatus status
) {
    public static StoreItemCreateResponse of(Long id, String name, StoreItemStatus status) {
        return new StoreItemCreateResponse(id, name, status);
    }
}
