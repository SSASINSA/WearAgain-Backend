package com.ssasinsa.wearagain.domain.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "댓글 리스트 응답")
public record CommentsResponse(
        @Schema(description = "한 번에 가져온 댓글 개수", example = "10")
        Integer limit,

        @Schema(description = "다음 요청 시 사용할 커서 (없으면 null)", example = "100", nullable = true)
        String nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        Boolean hasNext,

        @Schema(description = "댓글 목록")
        List<CommentItem> comments
) {
    @Schema(description = "댓글 항목")
    public record CommentItem(
            @Schema(description = "댓글 ID", example = "1")
            Long id,

            @Schema(description = "작성자 정보")
            AuthorInfo author,

            @Schema(description = "작성일 (ISO 8601 형식)", example = "2025-01-15T10:30:00")
            LocalDateTime createdAt,

            @Schema(description = "댓글 내용", example = "좋은 후기네요! 저도 다녀왔어요 😄")
            String content,

            @Schema(description = "내가 작성한 댓글인지 여부", example = "true")
            Boolean isMine
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

