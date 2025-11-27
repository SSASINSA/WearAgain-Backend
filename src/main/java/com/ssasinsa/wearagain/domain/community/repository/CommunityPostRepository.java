package com.ssasinsa.wearagain.domain.community.repository;

import com.ssasinsa.wearagain.domain.community.entity.CommentStatus;
import com.ssasinsa.wearagain.domain.community.entity.CommunityPost;
import com.ssasinsa.wearagain.domain.community.entity.PostStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    @Query("SELECT p FROM CommunityPost p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.id = :postId AND p.status != :excludedStatus")
    Optional<CommunityPost> findByIdAndStatusNot(@Param("postId") Long postId, @Param("excludedStatus") PostStatus excludedStatus);

    default Optional<CommunityPost> findByIdAndActiveTrue(Long postId) {
        return findByIdAndStatusNot(postId, PostStatus.INACTIVE);
    }

    @Query("SELECT COUNT(c) FROM PostComment c WHERE c.post.id = :postId AND c.status != :excludedStatus")
    long countActiveCommentsByPostId(@Param("postId") Long postId, @Param("excludedStatus") CommentStatus excludedStatus);

    default long countActiveCommentsByPostId(Long postId) {
        return countActiveCommentsByPostId(postId, CommentStatus.INACTIVE);
    }

    @Query("""
            SELECT c.post.id, COUNT(c)
            FROM PostComment c
            WHERE c.post.id IN :postIds AND c.status != :excludedStatus
            GROUP BY c.post.id
            """)
    List<Object[]> countActiveCommentsByPostIds(
            @Param("postIds") List<Long> postIds,
            @Param("excludedStatus") CommentStatus excludedStatus
    );

    default List<Object[]> countActiveCommentsByPostIds(List<Long> postIds) {
        return countActiveCommentsByPostIds(postIds, CommentStatus.INACTIVE);
    }

    @Query("SELECT p FROM CommunityPost p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.status != :excludedStatus " +
            "AND (:cursor IS NULL OR p.id < :cursor) " +
            "AND (:keyword IS NULL OR p.category.name = :keyword) " +
            "ORDER BY p.id DESC")
    List<CommunityPost> findPostsWithCursor(
            @Param("excludedStatus") PostStatus excludedStatus,
            @Param("cursor") Long cursor,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    default List<CommunityPost> findActivePostsWithCursor(Long cursor, String keyword, Pageable pageable) {
        return findPostsWithCursor(PostStatus.INACTIVE, cursor, keyword, pageable);
    }
}

