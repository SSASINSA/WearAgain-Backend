package com.ssasinsa.wearagain.domain.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 신고 요청")
public record ReportRequest(
        @NotNull
        @Schema(description = "게시글 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long postId,

        @NotBlank
        @Size(max = 255)
        @Schema(description = "신고 사유", example = "부적절한 언어 사용", requiredMode = Schema.RequiredMode.REQUIRED)
        String reason
) {
}

