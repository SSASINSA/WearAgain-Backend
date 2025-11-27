package com.ssasinsa.wearagain.domain.community.repository;

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
            WHERE c.post.id = :postId AND c.active = true
              AND (:cursor IS NULL OR c.id < :cursor)
            ORDER BY c.id DESC
            """)
    List<Long> findCommentIdsByPostId(
            @Param("postId") Long postId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT c FROM PostComment c
                LEFT JOIN FETCH c.user
            WHERE c.id IN :ids AND c.active = true
            ORDER BY c.id DESC
            """)
    List<PostComment> findCommentsByIds(@Param("ids") List<Long> ids);

    @Query("""
            SELECT c FROM PostComment c
                LEFT JOIN FETCH c.user
            WHERE c.id = :commentId AND c.active = true
            """)
    Optional<PostComment> findByIdAndActiveTrue(@Param("commentId") Long commentId);
}

