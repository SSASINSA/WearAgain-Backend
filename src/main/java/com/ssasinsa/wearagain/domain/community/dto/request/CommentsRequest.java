package com.ssasinsa.wearagain.domain.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 리스트 조회 요청")
public record CommentsRequest(
        @Schema(description = "마지막으로 조회한 댓글의 ID (없으면 첫 페이지)", example = "100", nullable = true)
        Long cursor,

        @Schema(description = "한 번에 가져올 댓글 개수 (기본값: 10)", example = "10", defaultValue = "10")
        Integer limit
) {
    public CommentsRequest {
        if (limit == null) {
            limit = 10;
        }
        if (limit <= 0 || limit > 50) {
            limit = 10;
        }
    }
}

