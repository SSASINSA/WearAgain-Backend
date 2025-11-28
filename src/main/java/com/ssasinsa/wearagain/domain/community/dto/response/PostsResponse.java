package com.ssasinsa.wearagain.domain.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "게시글 리스트 응답")
public record PostsResponse(
        @Schema(description = "한 번에 가져온 게시물 개수", example = "10")
        Integer limit,

        @Schema(description = "다음 요청 시 사용할 커서 (없으면 null)", example = "100", nullable = true)
        String nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        Boolean hasNext,

        @Schema(description = "게시물 목록")
        List<PostsItem> posts
) {
    @Schema(description = "게시물 목록 항목")
    public record PostsItem(
            @Schema(description = "게시물 ID", example = "1")
            Long id,

            @Schema(description = "이미지 주소 (첫 번째 이미지)", example = "https://cdn.wearagain.kr/community/posts/1/image1.jpg", nullable = true)
            String imageUrl,

            @Schema(description = "작성자 정보")
            AuthorInfo author,

            @Schema(description = "작성일 (ISO 8601 형식)", example = "2025-01-15T10:30:00")
            java.time.LocalDateTime createdAt,

            @Schema(description = "게시물 제목", example = "리폼 후기 공유합니다")
            String title,

            @Schema(description = "게시물 내용", example = "첫 리폼 경험을 공유하고 싶어서 글을 올립니다...")
            String content,

            @Schema(description = "좋아요 수", example = "15")
            Integer likeCount,

            @Schema(description = "댓글 수", example = "8")
            Integer commentCount,

            @Schema(description = "게시물 키워드", example = "리뷰")
            String keyword,

            @Schema(description = "내가 좋아요를 한 게시물인지 여부", example = "true")
            Boolean isLiked
    ) {
        @Schema(description = "작성자 정보")
        public record AuthorInfo(
                @Schema(description = "작성자 ID", example = "1")
                Long id,

                @Schema(description = "작성자 이름", example = "홍길동")
                String name
        ) {
        }
    }
}

