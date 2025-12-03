package com.ssasinsa.wearagain.domain.community.dto.admin;

import com.ssasinsa.wearagain.domain.community.entity.CommentStatus;
import com.ssasinsa.wearagain.domain.community.entity.PostStatus;
import java.time.LocalDateTime;
import java.util.List;

public record PostAdminDetailResponse(
        Long postId,
        PostStatus status,
        String title,
        String content,
        String categoryName,
        AuthorInfo author,
        List<String> imageUrls,
        int likeCount,
        int commentCount,
        int reportCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentInfo> comments
) {

    public record AuthorInfo(
            Long authorId,
            String displayName,
            String email
    ) {
    }

    public record CommentInfo(
            Long commentId,
            String content,
            CommentStatus status,
            CommentAuthorInfo author,
            LocalDateTime createdAt
    ) {
        public record CommentAuthorInfo(
                Long authorId,
                String displayName,
                String email
        ) {
        }
    }
}
