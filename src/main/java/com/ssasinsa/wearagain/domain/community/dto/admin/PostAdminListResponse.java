package com.ssasinsa.wearagain.domain.community.dto.admin;

import com.ssasinsa.wearagain.domain.community.entity.PostStatus;
import java.time.LocalDateTime;
import java.util.List;

public record PostAdminListResponse(
        List<PostAdminSummary> posts,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public record PostAdminSummary(
            Long postId,
            String title,
            PostStatus status,
            String categoryName,
            AuthorInfo author,
            int likeCount,
            int commentCount,
            int reportCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public record AuthorInfo(
                Long authorId,
                String displayName,
                String email
        ) {
        }
    }
}
