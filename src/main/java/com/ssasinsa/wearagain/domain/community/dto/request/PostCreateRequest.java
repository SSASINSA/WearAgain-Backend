package com.ssasinsa.wearagain.domain.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "게시글 생성 요청")
public record PostCreateRequest(
        @NotBlank
        @Size(max = 255)
        @Schema(description = "게시물 제목", example = "리폼 후기 공유합니다", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @NotBlank
        @Schema(description = "게시물 내용", example = "첫 리폼 경험을 공유하고 싶어서 글을 올립니다...", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,

        @NotBlank
        @Schema(description = "게시물 키워드 (질문, 후기, 수선)", example = "review", requiredMode = Schema.RequiredMode.REQUIRED)
        String keyword,

        @Schema(description = "이미지 URL 목록", example = "[\"https://cdn.wearagain.kr/community/posts/1/image1.jpg\"]")
        List<@Size(max = 512) String> imageUrls
) {
}

