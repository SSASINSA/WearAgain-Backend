package com.ssasinsa.wearagain.domain.store.dto.request;

import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "스토어 상품 수정 요청")
public record StoreItemUpdateRequest(
        @Schema(description = "상품명", example = "에코백(리디자인)")
        @Size(min = 1, max = 255)
        String name,

        @Schema(description = "상품 설명", example = "내구성을 높인 업사이클링 에코백입니다.")
        @Size(max = 4000)
        String description,

        @Schema(description = "카테고리", example = "bag")
        @Size(max = 50)
        String category,

        @Schema(description = "판매가", example = "15000")
        @Positive
        Integer price,

        @Schema(description = "재고 수량", example = "80")
        @PositiveOrZero
        Integer stock,

        @Schema(description = "사용자별 최대 구매 횟수", example = "1")
        @Positive
        Integer maxPurchasePerUser,

        @Schema(description = "상품 상태", example = "INACTIVE")
        StoreItemStatus status,

        @Schema(description = "상품 이미지 목록(전체 교체 시 사용)")
        List<@Valid StoreItemImageRequest> images
) {

    @Schema(description = "스토어 상품 이미지 정보")
    public record StoreItemImageRequest(
            @Schema(description = "이미지 URL", example = "https://cdn.wearagain.kr/store/items/1/main.jpg")
            @Size(min = 1, max = 512)
            String imageUrl,

            @Schema(description = "노출 순서(1부터 시작)", example = "1")
            @Positive
            Integer sortOrder
    ) {
    }
}
