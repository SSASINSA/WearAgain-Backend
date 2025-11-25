package com.ssasinsa.wearagain.domain.community.repository;

import com.ssasinsa.wearagain.domain.community.entity.CommunityPost;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    @Query("SELECT p FROM CommunityPost p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.id = :postId AND p.active = true")
    Optional<CommunityPost> findByIdAndActiveTrue(@Param("postId") Long postId);

    @Query("SELECT COUNT(c) FROM PostComment c WHERE c.post.id = :postId AND c.active = true")
    long countActiveCommentsByPostId(@Param("postId") Long postId);
}

