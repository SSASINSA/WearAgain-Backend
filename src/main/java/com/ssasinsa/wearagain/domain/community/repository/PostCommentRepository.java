package com.ssasinsa.wearagain.domain.community.repository;

import com.ssasinsa.wearagain.domain.community.entity.CommentStatus;
import com.ssasinsa.wearagain.domain.community.entity.PostComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query("""
            SELECT c.id FROM PostComment c
            WHERE c.post.id = :postId AND c.status != :excludedStatus
              AND (:cursor IS NULL OR c.id < :cursor)
            ORDER BY c.id DESC
            """)
    List<Long> findCommentIdsByPostId(
            @Param("postId") Long postId,
            @Param("excludedStatus") CommentStatus excludedStatus,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    default List<Long> findActiveCommentIdsByPostId(Long postId, Long cursor, Pageable pageable) {
        return findCommentIdsByPostId(postId, CommentStatus.INACTIVE, cursor, pageable);
    }

    @Query("""
            SELECT DISTINCT c FROM PostComment c
                LEFT JOIN FETCH c.user
            WHERE c.id IN :ids AND c.status != :excludedStatus
            ORDER BY c.id DESC
            """)
    List<PostComment> findCommentsByIds(
            @Param("ids") List<Long> ids,
            @Param("excludedStatus") CommentStatus excludedStatus
    );

    default List<PostComment> findActiveCommentsByIds(List<Long> ids) {
        return findCommentsByIds(ids, CommentStatus.INACTIVE);
    }

    @Query("""
            SELECT c FROM PostComment c
                LEFT JOIN FETCH c.user
            WHERE c.id = :commentId AND c.status != :excludedStatus
            """)
    Optional<PostComment> findByIdAndStatusNot(
            @Param("commentId") Long commentId,
            @Param("excludedStatus") CommentStatus excludedStatus
    );

    default Optional<PostComment> findByIdAndActiveTrue(Long commentId) {
        return findByIdAndStatusNot(commentId, CommentStatus.INACTIVE);
    }
}

