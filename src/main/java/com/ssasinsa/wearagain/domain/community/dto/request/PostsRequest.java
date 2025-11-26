package com.ssasinsa.wearagain.domain.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 리스트 조회 요청")
public record PostsRequest(
        @Schema(description = "마지막으로 조회한 게시물의 ID (없으면 첫 페이지)", example = "100", nullable = true)
        Long cursor,

        @Schema(description = "한 번에 가져올 게시물 개수 (기본값: 10)", example = "10", defaultValue = "10")
        Integer limit,

        @Schema(description = "키워드 필터 (question, review, repair)", example = "review", allowableValues = {"question", "review", "repair"}, nullable = true)
        String keyword
) {
    public PostsRequest {
        if (limit == null) {
            limit = 10;
        }
        if (limit <= 0 || limit > 50) {
            limit = 10;
        }
    }
}

