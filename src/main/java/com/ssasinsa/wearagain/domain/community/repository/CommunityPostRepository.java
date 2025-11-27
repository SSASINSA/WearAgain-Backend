package com.ssasinsa.wearagain.domain.community.repository;

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

    @Query("SELECT COUNT(c) FROM PostComment c WHERE c.post.id = :postId AND c.active = true")
    long countActiveCommentsByPostId(@Param("postId") Long postId);

    @Query("""
            SELECT c.post.id, COUNT(c)
            FROM PostComment c
            WHERE c.post.id IN :postIds AND c.active = true
            GROUP BY c.post.id
            """)
    List<Object[]> countActiveCommentsByPostIds(@Param("postIds") List<Long> postIds);

    @Query("""
            SELECT p.id FROM CommunityPost p
            WHERE p.status != :excludedStatus
              AND (:cursor IS NULL OR p.id < :cursor)
              AND (:keyword IS NULL OR p.category.name = :keyword)
            ORDER BY p.id DESC
            """)
    List<Long> findPostIdsForCursor(
            @Param("excludedStatus") PostStatus excludedStatus,
            @Param("cursor") Long cursor,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT p FROM CommunityPost p
                LEFT JOIN FETCH p.user
                LEFT JOIN FETCH p.category
            WHERE p.id IN :ids
            ORDER BY p.id DESC
            """)
    List<CommunityPost> findPostsByIds(@Param("ids") List<Long> ids);

    default List<Long> findActivePostIdsForCursor(Long cursor, String keyword, Pageable pageable) {
        return findPostIdsForCursor(PostStatus.INACTIVE, cursor, keyword, pageable);
    }
}

