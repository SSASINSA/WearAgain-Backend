package com.ssasinsa.wearagain.domain.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 좋아요 응답")
public record PostLikeResponse(
        @Schema(description = "좋아요 여부", example = "true")
        Boolean isLiked,

        @Schema(description = "좋아요 수", example = "15")
        Integer likeCount
) {
}

