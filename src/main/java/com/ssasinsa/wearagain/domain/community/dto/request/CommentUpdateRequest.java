package com.ssasinsa.wearagain.domain.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "댓글 수정 요청")
public record CommentUpdateRequest(
        @NotBlank
        @Schema(description = "댓글 내용", example = "좋은 후기네요! 저도 다녀왔어요 😄", requiredMode = Schema.RequiredMode.REQUIRED)
        String content
) {
}

